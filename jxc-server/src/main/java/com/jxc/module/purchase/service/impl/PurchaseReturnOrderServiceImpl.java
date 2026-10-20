package com.jxc.module.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.common.BusinessException;
import com.jxc.common.PageResult;
import com.jxc.common.ResultCode;
import com.jxc.context.UserContext;
import com.jxc.module.purchase.entity.PurchaseReturnOrder;
import com.jxc.module.purchase.entity.PurchaseReturnOrderDetail;
import com.jxc.module.purchase.mapper.PurchaseReturnOrderDetailMapper;
import com.jxc.module.purchase.mapper.PurchaseReturnOrderMapper;
import com.jxc.module.purchase.service.PurchaseReturnOrderService;
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
 * 采购退货服务实现
 * 规则:审核时扣减库存(退还给供应商,库存减少)、审核后不可改、事务控制
 *
 * @author jxc
 */
@Service
@RequiredArgsConstructor
public class PurchaseReturnOrderServiceImpl
        extends ServiceImpl<PurchaseReturnOrderMapper, PurchaseReturnOrder>
        implements PurchaseReturnOrderService {

    private final PurchaseReturnOrderDetailMapper detailMapper;
    private final StockService stockService;

    @Override
    public PageResult<PurchaseReturnOrder> page(Long page, Long size, String orderNo,
                                                Long supplierId, Long warehouseId, Integer auditStatus) {
        LambdaQueryWrapper<PurchaseReturnOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), PurchaseReturnOrder::getOrderNo, orderNo)
                .eq(supplierId != null, PurchaseReturnOrder::getSupplierId, supplierId)
                .eq(warehouseId != null, PurchaseReturnOrder::getWarehouseId, warehouseId)
                .eq(auditStatus != null, PurchaseReturnOrder::getAuditStatus, auditStatus)
                .orderByDesc(PurchaseReturnOrder::getCreateTime);
        IPage<PurchaseReturnOrder> p = new Page<>(page, size);
        IPage<PurchaseReturnOrder> result = page(p, wrapper);
        // 批量填充明细,供列表展示商品
        fillDetails(result.getRecords());
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    /**
     * 批量填充主表的明细列表(IN 查询一次取回所有明细)
     */
    private void fillDetails(List<PurchaseReturnOrder> orders) {
        if (CollectionUtils.isEmpty(orders)) return;
        List<Long> orderIds = orders.stream().map(PurchaseReturnOrder::getId).collect(Collectors.toList());
        List<PurchaseReturnOrderDetail> allDetails = detailMapper.selectList(
                new LambdaQueryWrapper<PurchaseReturnOrderDetail>().in(PurchaseReturnOrderDetail::getOrderId, orderIds));
        Map<Long, List<PurchaseReturnOrderDetail>> groupBy = allDetails.stream()
                .collect(Collectors.groupingBy(PurchaseReturnOrderDetail::getOrderId));
        for (PurchaseReturnOrder o : orders) {
            o.setDetails(groupBy.getOrDefault(o.getId(), Collections.emptyList()));
        }
    }

    @Override
    public PurchaseReturnOrder getDetail(Long id) {
        PurchaseReturnOrder order = getById(id);
        if (order == null) {
            return null;
        }
        List<PurchaseReturnOrderDetail> details = detailMapper.selectByOrderId(id);
        order.setDetails(details);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(PurchaseReturnOrder order) {
        if (CollectionUtils.isEmpty(order.getDetails())) {
            throw new BusinessException("退货明细不能为空");
        }
        order.setOrderNo(OrderNoGenerator.next("PR"));
        if (order.getOrderTime() == null) {
            order.setOrderTime(new Date());
        }
        order.setAuditStatus(0);
        calcTotal(order);
        save(order);
        for (PurchaseReturnOrderDetail d : order.getDetails()) {
            d.setOrderId(order.getId());
            d.setOrderNo(order.getOrderNo());
            d.setCreateTime(new Date());
            detailMapper.insert(d);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(PurchaseReturnOrder order) {
        PurchaseReturnOrder exist = getById(order.getId());
        if (exist == null) {
            throw new BusinessException("采购退货单不存在");
        }
        if (exist.getAuditStatus() != null && exist.getAuditStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_AUDITED);
        }
        order.setOrderNo(exist.getOrderNo());
        order.setAuditStatus(0);
        calcTotal(order);
        updateById(order);
        if (!CollectionUtils.isEmpty(order.getDetails())) {
            detailMapper.delete(new LambdaQueryWrapper<PurchaseReturnOrderDetail>()
                    .eq(PurchaseReturnOrderDetail::getOrderId, order.getId()));
            for (PurchaseReturnOrderDetail d : order.getDetails()) {
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
        PurchaseReturnOrder exist = getById(id);
        if (exist == null) {
            throw new BusinessException("采购退货单不存在");
        }
        if (exist.getAuditStatus() != null && exist.getAuditStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_AUDITED);
        }
        detailMapper.delete(new LambdaQueryWrapper<PurchaseReturnOrderDetail>()
                .eq(PurchaseReturnOrderDetail::getOrderId, id));
        removeById(id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, String remark) {
        PurchaseReturnOrder exist = getById(id);
        if (exist == null) {
            throw new BusinessException("采购退货单不存在");
        }
        if (exist.getAuditStatus() != null && exist.getAuditStatus() == 1) {
            throw new BusinessException("采购退货单已审核,请勿重复审核");
        }
        List<PurchaseReturnOrderDetail> details = detailMapper.selectByOrderId(id);
        if (CollectionUtils.isEmpty(details)) {
            throw new BusinessException("退货明细为空,无法审核");
        }
        // 逐条扣减库存(采购退货 = 退还给供应商,库存减少,任一失败整体回滚)
        for (PurchaseReturnOrderDetail d : details) {
            if (d.getQuantity() == null || d.getQuantity() <= 0) {
                throw new BusinessException("退货数量必须大于0");
            }
            stockService.deductStock(exist.getWarehouseId(), d.getProductId(), d.getQuantity());
        }
        PurchaseReturnOrder update = new PurchaseReturnOrder();
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
    private void calcTotal(PurchaseReturnOrder order) {
        if (CollectionUtils.isEmpty(order.getDetails())) {
            return;
        }
        int totalQty = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseReturnOrderDetail d : order.getDetails()) {
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
    public List<PurchaseReturnOrder> export(String orderNo, Long supplierId, Long warehouseId, Integer auditStatus) {
        LambdaQueryWrapper<PurchaseReturnOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), PurchaseReturnOrder::getOrderNo, orderNo)
                .eq(supplierId != null, PurchaseReturnOrder::getSupplierId, supplierId)
                .eq(warehouseId != null, PurchaseReturnOrder::getWarehouseId, warehouseId)
                .eq(auditStatus != null, PurchaseReturnOrder::getAuditStatus, auditStatus)
                .orderByDesc(PurchaseReturnOrder::getOrderTime);
        List<PurchaseReturnOrder> orders = list(wrapper);
        for (PurchaseReturnOrder order : orders) {
            order.setDetails(detailMapper.selectByOrderId(order.getId()));
        }
        return orders;
    }
}
