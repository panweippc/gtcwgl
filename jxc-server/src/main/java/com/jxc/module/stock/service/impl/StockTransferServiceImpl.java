package com.jxc.module.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.common.BusinessException;
import com.jxc.common.PageResult;
import com.jxc.common.ResultCode;
import com.jxc.context.UserContext;
import com.jxc.module.stock.entity.StockTransfer;
import com.jxc.module.stock.entity.StockTransferDetail;
import com.jxc.module.stock.mapper.StockTransferDetailMapper;
import com.jxc.module.stock.mapper.StockTransferMapper;
import com.jxc.module.stock.service.StockService;
import com.jxc.module.stock.service.StockTransferService;
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
 * 库存调拨服务实现
 * 规则:审核后单据不可改、出入库事务控制、禁止负库存
 *
 * @author jxc
 */
@Service
@RequiredArgsConstructor
public class StockTransferServiceImpl extends ServiceImpl<StockTransferMapper, StockTransfer> implements StockTransferService {

    private static final Integer AUDIT_STATUS_AUDITED = 1;
    private static final Integer AUDIT_STATUS_PENDING = 0;

    private final StockTransferDetailMapper detailMapper;
    private final StockService stockService;

    @Override
    public PageResult<StockTransfer> page(Long page, Long size, String orderNo,
                                          Long fromWarehouseId, Long toWarehouseId, Integer auditStatus) {
        LambdaQueryWrapper<StockTransfer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(hasText(orderNo), StockTransfer::getOrderNo, orderNo)
                .eq(fromWarehouseId != null, StockTransfer::getFromWarehouseId, fromWarehouseId)
                .eq(toWarehouseId != null, StockTransfer::getToWarehouseId, toWarehouseId)
                .eq(auditStatus != null, StockTransfer::getAuditStatus, auditStatus)
                .orderByDesc(StockTransfer::getCreateTime);
        IPage<StockTransfer> p = new Page<>(page, size);
        IPage<StockTransfer> result = page(p, wrapper);
        // 批量填充明细,供列表展示调拨商品
        fillDetails(result.getRecords());
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public List<StockTransfer> export(String orderNo, Long fromWarehouseId, Long toWarehouseId, Integer auditStatus) {
        LambdaQueryWrapper<StockTransfer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(hasText(orderNo), StockTransfer::getOrderNo, orderNo)
                .eq(fromWarehouseId != null, StockTransfer::getFromWarehouseId, fromWarehouseId)
                .eq(toWarehouseId != null, StockTransfer::getToWarehouseId, toWarehouseId)
                .eq(auditStatus != null, StockTransfer::getAuditStatus, auditStatus)
                .orderByDesc(StockTransfer::getOrderTime);
        List<StockTransfer> transfers = list(wrapper);
        // 批量填充明细
        fillDetails(transfers);
        return transfers;
    }

    /**
     * 批量填充明细(IN 查询一次取回所有明细)
     */
    private void fillDetails(List<StockTransfer> transfers) {
        if (CollectionUtils.isEmpty(transfers)) return;
        List<Long> orderIds = transfers.stream().map(StockTransfer::getId).collect(Collectors.toList());
        List<StockTransferDetail> allDetails = detailMapper.selectList(
                new LambdaQueryWrapper<StockTransferDetail>().in(StockTransferDetail::getOrderId, orderIds));
        Map<Long, List<StockTransferDetail>> groupBy = allDetails.stream()
                .collect(Collectors.groupingBy(StockTransferDetail::getOrderId));
        for (StockTransfer t : transfers) {
            t.setDetails(groupBy.getOrDefault(t.getId(), Collections.emptyList()));
        }
    }

    @Override
    public StockTransfer getDetail(Long id) {
        StockTransfer transfer = getById(id);
        if (transfer == null) {
            return null;
        }
        List<StockTransferDetail> details = detailMapper.selectByOrderId(id);
        transfer.setDetails(details);
        return transfer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(StockTransfer transfer) {
        // 校验仓库和明细
        validateWarehouse(transfer);
        List<StockTransferDetail> details = transfer.getDetails();
        validateDetails(details);
        // 生成单号
        transfer.setOrderNo(OrderNoGenerator.next("TR"));
        if (transfer.getOrderTime() == null) {
            transfer.setOrderTime(new Date());
        }
        transfer.setAuditStatus(AUDIT_STATUS_PENDING);
        transfer.setTotalQuantity(calcTotal(details));
        // 保存主表
        save(transfer);
        // 保存明细
        saveDetails(transfer, details);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(StockTransfer transfer) {
        StockTransfer exist = getById(transfer.getId());
        if (exist == null) {
            throw new BusinessException("调拨单不存在");
        }
        checkNotAudited(exist.getAuditStatus());
        // 校验仓库
        validateWarehouse(transfer);
        // 校验并重算明细
        List<StockTransferDetail> details = transfer.getDetails();
        if (!CollectionUtils.isEmpty(details)) {
            validateDetails(details);
            transfer.setTotalQuantity(calcTotal(details));
        }
        // 更新主表(不修改单号和审核状态)
        transfer.setOrderNo(exist.getOrderNo());
        transfer.setAuditStatus(AUDIT_STATUS_PENDING);
        updateById(transfer);
        // 删除原明细,重新插入
        if (!CollectionUtils.isEmpty(details)) {
            detailMapper.delete(new LambdaQueryWrapper<StockTransferDetail>()
                    .eq(StockTransferDetail::getOrderId, transfer.getId()));
            saveDetails(transfer, details);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        StockTransfer exist = getById(id);
        if (exist == null) {
            throw new BusinessException("调拨单不存在");
        }
        checkNotAudited(exist.getAuditStatus());
        // 删除明细
        detailMapper.delete(new LambdaQueryWrapper<StockTransferDetail>()
                .eq(StockTransferDetail::getOrderId, id));
        // 删除主表
        removeById(id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, String remark) {
        StockTransfer exist = getById(id);
        if (exist == null) {
            throw new BusinessException("调拨单不存在");
        }
        if (isAudited(exist.getAuditStatus())) {
            throw new BusinessException("调拨单已审核,请勿重复审核");
        }
        // 查询明细
        List<StockTransferDetail> details = detailMapper.selectByOrderId(id);
        if (CollectionUtils.isEmpty(details)) {
            throw new BusinessException("调拨明细为空,无法审核");
        }
        // 逐条执行库存调拨(任一条库存不足则整体回滚)
        for (StockTransferDetail d : details) {
            Integer quantity = d.getQuantity();
            if (quantity == null || quantity <= 0) {
                throw new BusinessException("调拨数量必须大于0");
            }
            stockService.transferStock(exist.getFromWarehouseId(), exist.getToWarehouseId(),
                    d.getProductId(), quantity);
        }
        // 更新审核状态
        StockTransfer update = new StockTransfer();
        update.setId(id);
        update.setAuditStatus(AUDIT_STATUS_AUDITED);
        update.setAuditUserId(UserContext.getCurrentUserId());
        update.setAuditTime(new Date());
        if (hasText(remark)) {
            update.setRemark(remark);
        }
        updateById(update);
    }

    /**
     * 校验仓库:调出/调入仓库不能为空且不能相同
     *
     * @param transfer 调拨单
     */
    private void validateWarehouse(StockTransfer transfer) {
        if (transfer.getFromWarehouseId() == null) {
            throw new BusinessException("请选择调出仓库");
        }
        if (transfer.getToWarehouseId() == null) {
            throw new BusinessException("请选择调入仓库");
        }
        if (Objects.equals(transfer.getFromWarehouseId(), transfer.getToWarehouseId())) {
            throw new BusinessException("调出仓库与调入仓库不能相同");
        }
    }

    /**
     * 校验明细:不能为空且每条数量必须大于0
     *
     * @param details 明细列表
     */
    private void validateDetails(List<StockTransferDetail> details) {
        if (CollectionUtils.isEmpty(details)) {
            throw new BusinessException("调拨明细不能为空");
        }
        for (StockTransferDetail d : details) {
            Integer quantity = d.getQuantity();
            if (quantity == null || quantity <= 0) {
                throw new BusinessException("调拨数量必须大于0");
            }
        }
    }

    /**
     * 计算明细总数量
     *
     * @param details 明细列表
     * @return 总数量
     */
    private int calcTotal(List<StockTransferDetail> details) {
        return details.stream()
                .mapToInt(StockTransferDetail::getQuantity)
                .sum();
    }

    /**
     * 保存明细列表
     *
     * @param transfer 调拨主表(用于取 id 和 orderNo)
     * @param details  明细列表
     */
    private void saveDetails(StockTransfer transfer, List<StockTransferDetail> details) {
        Date now = new Date();
        for (StockTransferDetail d : details) {
            d.setOrderId(transfer.getId());
            d.setOrderNo(transfer.getOrderNo());
            d.setCreateTime(now);
            detailMapper.insert(d);
        }
    }

    /**
     * 检查单据是否已审核,已审核则抛异常
     *
     * @param auditStatus 审核状态
     */
    private void checkNotAudited(Integer auditStatus) {
        if (isAudited(auditStatus)) {
            throw new BusinessException(ResultCode.ORDER_AUDITED);
        }
    }

    /**
     * 判断单据是否已审核
     *
     * @param auditStatus 审核状态
     * @return true-已审核
     */
    private boolean isAudited(Integer auditStatus) {
        return Objects.equals(auditStatus, AUDIT_STATUS_AUDITED);
    }

    /**
     * 判断字符串是否有内容(替代 StringUtils.hasText 避免静态导入)
     *
     * @param str 字符串
     * @return true-非空
     */
    private static boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
