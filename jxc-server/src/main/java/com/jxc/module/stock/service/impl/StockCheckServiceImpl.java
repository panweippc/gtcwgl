package com.jxc.module.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.common.BusinessException;
import com.jxc.common.PageResult;
import com.jxc.common.ResultCode;
import com.jxc.context.UserContext;
import com.jxc.module.stock.entity.StockCheck;
import com.jxc.module.stock.entity.StockCheckDetail;
import com.jxc.module.stock.mapper.StockCheckDetailMapper;
import com.jxc.module.stock.mapper.StockCheckMapper;
import com.jxc.module.stock.mapper.StockMapper;
import com.jxc.module.stock.service.StockCheckService;
import com.jxc.module.stock.service.StockService;
import com.jxc.module.stock.vo.StockBookVO;
import com.jxc.utils.OrderNoGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 库存盘点服务实现
 * 规则:盘点后单据不可改、库存增减事务控制、禁止负库存、全部差异生成库存流水留痕
 *
 * @author jxc
 */
@Service
@RequiredArgsConstructor
public class StockCheckServiceImpl extends ServiceImpl<StockCheckMapper, StockCheck> implements StockCheckService {

    /** 已盘点 */
    private static final Integer AUDIT_STATUS_AUDITED = 1;

    /** 待盘点 */
    private static final Integer AUDIT_STATUS_PENDING = 0;

    private final StockCheckDetailMapper detailMapper;
    private final StockMapper stockMapper;
    private final StockService stockService;

    @Override
    public PageResult<StockCheck> page(Long page, Long size, String orderNo, Long warehouseId, Integer auditStatus) {
        LambdaQueryWrapper<StockCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(hasText(orderNo), StockCheck::getOrderNo, orderNo)
                .eq(warehouseId != null, StockCheck::getWarehouseId, warehouseId)
                .eq(auditStatus != null, StockCheck::getAuditStatus, auditStatus)
                .orderByDesc(StockCheck::getCreateTime);
        IPage<StockCheck> p = new Page<>(page, size);
        IPage<StockCheck> result = page(p, wrapper);
        // 批量填充明细,供列表展示盘点商品
        fillDetails(result.getRecords());
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public List<StockCheck> export(String orderNo, Long warehouseId, Integer auditStatus) {
        LambdaQueryWrapper<StockCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(hasText(orderNo), StockCheck::getOrderNo, orderNo)
                .eq(warehouseId != null, StockCheck::getWarehouseId, warehouseId)
                .eq(auditStatus != null, StockCheck::getAuditStatus, auditStatus)
                .orderByDesc(StockCheck::getOrderTime);
        List<StockCheck> checks = list(wrapper);
        // 批量填充明细
        fillDetails(checks);
        return checks;
    }

    /**
     * 批量填充明细(IN 查询一次取回所有明细)
     *
     * @param checks 盘点单列表
     */
    private void fillDetails(List<StockCheck> checks) {
        if (CollectionUtils.isEmpty(checks)) {
            return;
        }
        List<Long> orderIds = checks.stream().map(StockCheck::getId).collect(Collectors.toList());
        List<StockCheckDetail> allDetails = detailMapper.selectList(
                new LambdaQueryWrapper<StockCheckDetail>().in(StockCheckDetail::getOrderId, orderIds));
        Map<Long, List<StockCheckDetail>> groupBy = allDetails.stream()
                .collect(Collectors.groupingBy(StockCheckDetail::getOrderId));
        for (StockCheck c : checks) {
            c.setDetails(groupBy.getOrDefault(c.getId(), Collections.emptyList()));
        }
    }

    @Override
    public StockCheck getDetail(Long id) {
        StockCheck check = getById(id);
        if (check == null) {
            return null;
        }
        check.setDetails(detailMapper.selectByOrderId(id));
        return check;
    }

    @Override
    public List<StockBookVO> bookList(Long warehouseId) {
        if (warehouseId == null) {
            throw new BusinessException("请选择仓库");
        }
        return stockMapper.selectBookList(warehouseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(StockCheck check) {
        validateWarehouse(check);
        List<StockCheckDetail> details = check.getDetails();
        validateDetails(details);
        // 生成盘点单号
        check.setOrderNo(OrderNoGenerator.next("PC"));
        if (check.getOrderTime() == null) {
            check.setOrderTime(new Date());
        }
        check.setAuditStatus(AUDIT_STATUS_PENDING);
        // 计算差异与汇总
        fillDifference(details);
        calcTotals(check, details);
        // 保存主表
        save(check);
        // 保存明细
        saveDetails(check, details);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(StockCheck check) {
        StockCheck exist = getById(check.getId());
        if (exist == null) {
            throw new BusinessException("盘点单不存在");
        }
        checkNotAudited(exist.getAuditStatus());
        validateWarehouse(check);
        List<StockCheckDetail> details = check.getDetails();
        validateDetails(details);
        // 重算差异与汇总
        fillDifference(details);
        calcTotals(check, details);
        // 更新主表(不修改单号、盘点状态与盘盈/盘亏单号)
        check.setOrderNo(exist.getOrderNo());
        check.setAuditStatus(AUDIT_STATUS_PENDING);
        check.setProfitOrderNo(exist.getProfitOrderNo());
        check.setLossOrderNo(exist.getLossOrderNo());
        updateById(check);
        // 删除原明细,重新插入
        detailMapper.delete(new LambdaQueryWrapper<StockCheckDetail>()
                .eq(StockCheckDetail::getOrderId, check.getId()));
        saveDetails(check, details);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        StockCheck exist = getById(id);
        if (exist == null) {
            throw new BusinessException("盘点单不存在");
        }
        checkNotAudited(exist.getAuditStatus());
        // 删除明细
        detailMapper.delete(new LambdaQueryWrapper<StockCheckDetail>()
                .eq(StockCheckDetail::getOrderId, id));
        // 删除主表
        removeById(id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, String remark) {
        StockCheck exist = getById(id);
        if (exist == null) {
            throw new BusinessException("盘点单不存在");
        }
        if (isAudited(exist.getAuditStatus())) {
            throw new BusinessException("盘点单已盘点,请勿重复盘点");
        }
        // 查询明细
        List<StockCheckDetail> details = detailMapper.selectByOrderId(id);
        if (CollectionUtils.isEmpty(details)) {
            throw new BusinessException("盘点明细为空,无法盘点");
        }
        // 汇总差异(仅统计已录入实盘数量的行)
        int profitTotal = 0;
        int lossTotal = 0;
        for (StockCheckDetail d : details) {
            int diff = calcDifference(d);
            if (diff > 0) {
                profitTotal += diff;
            } else if (diff < 0) {
                lossTotal += -diff;
            }
        }
        // 无差异时不生成盘盈/盘亏单,仅标记盘点完成
        String profitOrderNo = null;
        String lossOrderNo = null;
        // 盘盈:逐条增加库存
        if (profitTotal > 0) {
            profitOrderNo = OrderNoGenerator.next("PY");
            for (StockCheckDetail d : details) {
                int diff = calcDifference(d);
                if (diff > 0) {
                    stockService.addStock(exist.getWarehouseId(), d.getProductId(), diff);
                }
            }
        }
        // 盘亏:逐条扣减库存(禁止负库存)
        if (lossTotal > 0) {
            lossOrderNo = OrderNoGenerator.next("PL");
            for (StockCheckDetail d : details) {
                int diff = calcDifference(d);
                if (diff < 0) {
                    stockService.deductStock(exist.getWarehouseId(), d.getProductId(), -diff);
                }
            }
            // 扣减后再校验:理论上不会负库存,由原子SQL保证
        }
        // 更新盘点状态与盘盈/盘亏单号
        StockCheck update = new StockCheck();
        update.setId(id);
        update.setAuditStatus(AUDIT_STATUS_AUDITED);
        update.setAuditUserId(UserContext.getCurrentUserId());
        update.setAuditTime(new Date());
        update.setProfitOrderNo(profitOrderNo);
        update.setLossOrderNo(lossOrderNo);
        update.setTotalProfitQuantity(profitTotal);
        update.setTotalLossQuantity(lossTotal);
        if (hasText(remark)) {
            update.setRemark(remark);
        }
        updateById(update);
    }

    /**
     * 校验仓库
     *
     * @param check 盘点单
     */
    private void validateWarehouse(StockCheck check) {
        if (check.getWarehouseId() == null) {
            throw new BusinessException("请选择仓库");
        }
    }

    /**
     * 校验明细:不能为空、商品必填、实盘数量不能为负、至少录入一条实盘数量
     *
     * @param details 明细列表
     */
    private void validateDetails(List<StockCheckDetail> details) {
        if (CollectionUtils.isEmpty(details)) {
            throw new BusinessException("盘点明细不能为空,请先带出账面库存");
        }
        boolean hasActual = false;
        for (StockCheckDetail d : details) {
            if (d.getProductId() == null) {
                throw new BusinessException("盘点明细存在未选择商品的记录");
            }
            if (d.getActualQuantity() != null) {
                if (d.getActualQuantity() < 0) {
                    throw new BusinessException("实盘数量不能为负数");
                }
                hasActual = true;
            }
        }
        if (!hasActual) {
            throw new BusinessException("请至少录入一条实盘数量");
        }
    }

    /**
     * 计算单条明细的差异(实盘-账面), 未录入实盘数量返回0
     *
     * @param d 明细
     * @return 差异数量
     */
    private int calcDifference(StockCheckDetail d) {
        if (d.getActualQuantity() == null) {
            return 0;
        }
        int book = d.getBookQuantity() == null ? 0 : d.getBookQuantity();
        return d.getActualQuantity() - book;
    }

    /**
     * 回填每条明细的差异数量
     *
     * @param details 明细列表
     */
    private void fillDifference(List<StockCheckDetail> details) {
        for (StockCheckDetail d : details) {
            d.setDifference(calcDifference(d));
        }
    }

    /**
     * 计算主表汇总:账面、实盘、盘盈、盘亏(仅统计已录入实盘数量的行)
     *
     * @param check   盘点单
     * @param details 明细列表
     */
    private void calcTotals(StockCheck check, List<StockCheckDetail> details) {
        int bookTotal = 0;
        int actualTotal = 0;
        int profitTotal = 0;
        int lossTotal = 0;
        for (StockCheckDetail d : details) {
            if (d.getActualQuantity() == null) {
                continue;
            }
            int book = d.getBookQuantity() == null ? 0 : d.getBookQuantity();
            int diff = d.getActualQuantity() - book;
            bookTotal += book;
            actualTotal += d.getActualQuantity();
            if (diff > 0) {
                profitTotal += diff;
            } else if (diff < 0) {
                lossTotal += -diff;
            }
        }
        check.setTotalBookQuantity(bookTotal);
        check.setTotalActualQuantity(actualTotal);
        check.setTotalProfitQuantity(profitTotal);
        check.setTotalLossQuantity(lossTotal);
    }

    /**
     * 保存明细列表
     *
     * @param check   盘点主表(用于取 id 和 orderNo)
     * @param details 明细列表
     */
    private void saveDetails(StockCheck check, List<StockCheckDetail> details) {
        Date now = new Date();
        for (StockCheckDetail d : details) {
            d.setId(null);
            d.setOrderId(check.getId());
            d.setOrderNo(check.getOrderNo());
            d.setCreateTime(now);
            detailMapper.insert(d);
        }
    }

    /**
     * 检查单据是否已盘点,已盘点则抛异常
     *
     * @param auditStatus 盘点状态
     */
    private void checkNotAudited(Integer auditStatus) {
        if (isAudited(auditStatus)) {
            throw new BusinessException(ResultCode.ORDER_AUDITED);
        }
    }

    /**
     * 判断单据是否已盘点
     *
     * @param auditStatus 盘点状态
     * @return true-已盘点
     */
    private boolean isAudited(Integer auditStatus) {
        return Objects.equals(auditStatus, AUDIT_STATUS_AUDITED);
    }

    /**
     * 判断字符串是否有内容
     *
     * @param str 字符串
     * @return true-非空
     */
    private static boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
