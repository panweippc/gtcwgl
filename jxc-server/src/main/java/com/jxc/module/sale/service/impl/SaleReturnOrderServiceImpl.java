package com.jxc.module.sale.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.common.BusinessException;
import com.jxc.common.PageResult;
import com.jxc.common.ResultCode;
import com.jxc.context.UserContext;
import com.jxc.module.sale.entity.SaleReturnOrder;
import com.jxc.module.sale.entity.SaleReturnOrderDetail;
import com.jxc.module.sale.mapper.SaleReturnOrderDetailMapper;
import com.jxc.module.sale.mapper.SaleReturnOrderMapper;
import com.jxc.module.sale.service.SaleReturnOrderService;
import com.jxc.module.stock.service.StockService;
import com.jxc.utils.OrderNoGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 销售退货服务实现
 * 规则:审核时增加库存(客户退回,库存增加)、审核后不可改、事务控制
 *
 * @author jxc
 */
@Service
@RequiredArgsConstructor
public class SaleReturnOrderServiceImpl
        extends ServiceImpl<SaleReturnOrderMapper, SaleReturnOrder>
        implements SaleReturnOrderService {

    private final SaleReturnOrderDetailMapper detailMapper;
    private final StockService stockService;

    @Override
    public PageResult<SaleReturnOrder> page(Long page, Long size, String orderNo,
                                            Long customerId, Long warehouseId, Integer auditStatus) {
        LambdaQueryWrapper<SaleReturnOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), SaleReturnOrder::getOrderNo, orderNo)
                .eq(customerId != null, SaleReturnOrder::getCustomerId, customerId)
                .eq(warehouseId != null, SaleReturnOrder::getWarehouseId, warehouseId)
                .eq(auditStatus != null, SaleReturnOrder::getAuditStatus, auditStatus)
                .orderByDesc(SaleReturnOrder::getCreateTime);
        IPage<SaleReturnOrder> p = new Page<>(page, size);
        IPage<SaleReturnOrder> result = page(p, wrapper);
        // 批量填充明细,供列表展示商品
        fillDetails(result.getRecords());
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    /**
     * 批量填充明细(IN 查询一次取回所有明细)
     */
    private void fillDetails(List<SaleReturnOrder> orders) {
        if (CollectionUtils.isEmpty(orders)) return;
        List<Long> orderIds = orders.stream().map(SaleReturnOrder::getId).collect(Collectors.toList());
        List<SaleReturnOrderDetail> allDetails = detailMapper.selectList(
                new LambdaQueryWrapper<SaleReturnOrderDetail>().in(SaleReturnOrderDetail::getOrderId, orderIds));
        Map<Long, List<SaleReturnOrderDetail>> groupBy = allDetails.stream()
                .collect(Collectors.groupingBy(SaleReturnOrderDetail::getOrderId));
        for (SaleReturnOrder o : orders) {
            o.setDetails(groupBy.getOrDefault(o.getId(), Collections.emptyList()));
        }
    }

    @Override
    public SaleReturnOrder getDetail(Long id) {
        SaleReturnOrder order = getById(id);
        if (order == null) {
            return null;
        }
        List<SaleReturnOrderDetail> details = detailMapper.selectByOrderId(id);
        order.setDetails(details);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(SaleReturnOrder order) {
        if (CollectionUtils.isEmpty(order.getDetails())) {
            throw new BusinessException("退货明细不能为空");
        }
        order.setOrderNo(OrderNoGenerator.next("SR"));
        if (order.getOrderTime() == null) {
            order.setOrderTime(new Date());
        }
        order.setAuditStatus(0);
        calcTotal(order);
        save(order);
        for (SaleReturnOrderDetail d : order.getDetails()) {
            d.setOrderId(order.getId());
            d.setOrderNo(order.getOrderNo());
            d.setCreateTime(new Date());
            detailMapper.insert(d);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SaleReturnOrder order) {
        SaleReturnOrder exist = getById(order.getId());
        if (exist == null) {
            throw new BusinessException("销售退货单不存在");
        }
        if (exist.getAuditStatus() != null && exist.getAuditStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_AUDITED);
        }
        order.setOrderNo(exist.getOrderNo());
        order.setAuditStatus(0);
        calcTotal(order);
        updateById(order);
        if (!CollectionUtils.isEmpty(order.getDetails())) {
            detailMapper.delete(new LambdaQueryWrapper<SaleReturnOrderDetail>()
                    .eq(SaleReturnOrderDetail::getOrderId, order.getId()));
            for (SaleReturnOrderDetail d : order.getDetails()) {
                d.setOrderId(order.getId());
                d.setOrderNo(exist.getOrderNo());
                d.setCreateTime(new Date());
                detailMapper.insert(d);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        SaleReturnOrder exist = getById(id);
        if (exist == null) {
            throw new BusinessException("销售退货单不存在");
        }
        if (exist.getAuditStatus() != null && exist.getAuditStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_AUDITED);
        }
        detailMapper.delete(new LambdaQueryWrapper<SaleReturnOrderDetail>()
                .eq(SaleReturnOrderDetail::getOrderId, id));
        removeById(id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, String remark) {
        SaleReturnOrder exist = getById(id);
        if (exist == null) {
            throw new BusinessException("销售退货单不存在");
        }
        if (exist.getAuditStatus() != null && exist.getAuditStatus() == 1) {
            throw new BusinessException("销售退货单已审核,请勿重复审核");
        }
        List<SaleReturnOrderDetail> details = detailMapper.selectByOrderId(id);
        if (CollectionUtils.isEmpty(details)) {
            throw new BusinessException("退货明细为空,无法审核");
        }
        // 逐条增加库存(销售退货 = 客户退回,库存增加,任一失败整体回滚)
        for (SaleReturnOrderDetail d : details) {
            if (d.getQuantity() == null || d.getQuantity() <= 0) {
                throw new BusinessException("退货数量必须大于0");
            }
            stockService.addStock(exist.getWarehouseId(), d.getProductId(), d.getQuantity());
        }
        SaleReturnOrder update = new SaleReturnOrder();
        update.setId(id);
        update.setAuditStatus(1);
        update.setAuditUserId(UserContext.getCurrentUserId());
        update.setAuditTime(new Date());
        if (StringUtils.hasText(remark)) {
            update.setRemark(remark);
        }
        updateById(update);
    }

    /**
     * 计算合计金额与数量
     */
    private void calcTotal(SaleReturnOrder order) {
        if (CollectionUtils.isEmpty(order.getDetails())) {
            return;
        }
        int totalQty = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SaleReturnOrderDetail d : order.getDetails()) {
            if (d.getQuantity() != null && d.getPrice() != null) {
                // 金额按录入数量(所选单位单价)计算; 无录入数量时退化为按基本单位数量
                int inputQty = d.getInputQuantity() != null ? d.getInputQuantity() : d.getQuantity();
                BigDecimal amount = d.getPrice().multiply(new BigDecimal(inputQty));
                d.setAmount(amount);
                // 汇总数量按换算后的基本单位数量
                totalQty += d.getQuantity();
                totalAmount = totalAmount.add(amount);
            }
        }
        order.setTotalQuantity(totalQty);
        order.setTotalAmount(totalAmount);
        order.setRefundAmount(totalAmount);
    }

    @Override
    public List<SaleReturnOrder> export(String orderNo, Long customerId, Long warehouseId, Integer auditStatus) {
        LambdaQueryWrapper<SaleReturnOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), SaleReturnOrder::getOrderNo, orderNo)
                .eq(customerId != null, SaleReturnOrder::getCustomerId, customerId)
                .eq(warehouseId != null, SaleReturnOrder::getWarehouseId, warehouseId)
                .eq(auditStatus != null, SaleReturnOrder::getAuditStatus, auditStatus)
                .orderByDesc(SaleReturnOrder::getOrderTime);
        List<SaleReturnOrder> orders = list(wrapper);
        for (SaleReturnOrder order : orders) {
            order.setDetails(detailMapper.selectByOrderId(order.getId()));
        }
        return orders;
    }
}
