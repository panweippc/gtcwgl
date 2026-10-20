package com.jxc.module.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.common.BusinessException;
import com.jxc.common.PageResult;
import com.jxc.common.ResultCode;
import com.jxc.context.UserContext;
import com.jxc.module.purchase.entity.PurchaseOrder;
import com.jxc.module.purchase.entity.PurchaseOrderDetail;
import com.jxc.module.purchase.mapper.PurchaseOrderDetailMapper;
import com.jxc.module.purchase.mapper.PurchaseOrderMapper;
import com.jxc.module.purchase.service.PurchaseOrderService;
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
 * 采购入库服务实现
 * 规则:审核后单据不可改、审核时增加库存、出入库事务控制
 *
 * @author jxc
 */
@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseOrderService {

    private final PurchaseOrderDetailMapper detailMapper;
    private final StockService stockService;

    @Override
    public PageResult<PurchaseOrder> page(Long page, Long size, String orderNo,
                                          Long supplierId, Long warehouseId, Integer auditStatus) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), PurchaseOrder::getOrderNo, orderNo)
                .eq(supplierId != null, PurchaseOrder::getSupplierId, supplierId)
                .eq(warehouseId != null, PurchaseOrder::getWarehouseId, warehouseId)
                .eq(auditStatus != null, PurchaseOrder::getAuditStatus, auditStatus)
                .orderByDesc(PurchaseOrder::getCreateTime);
        IPage<PurchaseOrder> p = new Page<>(page, size);
        IPage<PurchaseOrder> result = page(p, wrapper);
        // 批量填充明细,供列表展示商品
        fillDetails(result.getRecords());
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    /**
     * 批量填充主表的明细列表(用于分页后 N+1 优化:IN 查询一次取回所有明细)
     */
    private void fillDetails(List<PurchaseOrder> orders) {
        if (CollectionUtils.isEmpty(orders)) return;
        List<Long> orderIds = orders.stream().map(PurchaseOrder::getId).collect(Collectors.toList());
        List<PurchaseOrderDetail> allDetails = detailMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderDetail>().in(PurchaseOrderDetail::getOrderId, orderIds));
        Map<Long, List<PurchaseOrderDetail>> groupBy = allDetails.stream()
                .collect(Collectors.groupingBy(PurchaseOrderDetail::getOrderId));
        for (PurchaseOrder o : orders) {
            o.setDetails(groupBy.getOrDefault(o.getId(), Collections.emptyList()));
        }
    }

    @Override
    public PurchaseOrder getDetail(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            return null;
        }
        List<PurchaseOrderDetail> details = detailMapper.selectByOrderId(id);
        order.setDetails(details);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(PurchaseOrder order) {
        if (CollectionUtils.isEmpty(order.getDetails())) {
            throw new BusinessException("采购明细不能为空");
        }
        order.setOrderNo(OrderNoGenerator.next("PO"));
        if (order.getOrderTime() == null) {
            order.setOrderTime(new Date());
        }
        order.setAuditStatus(0);
        // 计算合计
        calcTotal(order);
        save(order);
        for (PurchaseOrderDetail d : order.getDetails()) {
            d.setOrderId(order.getId());
            d.setOrderNo(order.getOrderNo());
            d.setCreateTime(new Date());
            detailMapper.insert(d);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(PurchaseOrder order) {
        PurchaseOrder exist = getById(order.getId());
        if (exist == null) {
            throw new BusinessException("采购单不存在");
        }
        if (exist.getAuditStatus() != null && exist.getAuditStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_AUDITED);
        }
        order.setOrderNo(exist.getOrderNo());
        order.setAuditStatus(0);
        calcTotal(order);
        updateById(order);
        if (!CollectionUtils.isEmpty(order.getDetails())) {
            detailMapper.delete(new LambdaQueryWrapper<PurchaseOrderDetail>()
                    .eq(PurchaseOrderDetail::getOrderId, order.getId()));
            for (PurchaseOrderDetail d : order.getDetails()) {
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
        PurchaseOrder exist = getById(id);
        if (exist == null) {
            throw new BusinessException("采购单不存在");
        }
        if (exist.getAuditStatus() != null && exist.getAuditStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_AUDITED);
        }
        detailMapper.delete(new LambdaQueryWrapper<PurchaseOrderDetail>()
                .eq(PurchaseOrderDetail::getOrderId, id));
        removeById(id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, String remark) {
        PurchaseOrder exist = getById(id);
        if (exist == null) {
            throw new BusinessException("采购单不存在");
        }
        if (exist.getAuditStatus() != null && exist.getAuditStatus() == 1) {
            throw new BusinessException("采购单已审核,请勿重复审核");
        }
        List<PurchaseOrderDetail> details = detailMapper.selectByOrderId(id);
        if (CollectionUtils.isEmpty(details)) {
            throw new BusinessException("采购明细为空,无法审核");
        }
        // 逐条增加库存(任一失败整体回滚)
        for (PurchaseOrderDetail d : details) {
            if (d.getQuantity() == null || d.getQuantity() <= 0) {
                throw new BusinessException("采购数量必须大于0");
            }
            stockService.addStock(exist.getWarehouseId(), d.getProductId(), d.getQuantity());
        }
        // 更新审核状态
        PurchaseOrder update = new PurchaseOrder();
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
     *
     * @param order 采购单
     */
    private void calcTotal(PurchaseOrder order) {
        if (CollectionUtils.isEmpty(order.getDetails())) {
            return;
        }
        int totalQty = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderDetail d : order.getDetails()) {
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
    }

    @Override
    public List<PurchaseOrder> export(String orderNo, Long supplierId, Long warehouseId, Integer auditStatus) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), PurchaseOrder::getOrderNo, orderNo)
                .eq(supplierId != null, PurchaseOrder::getSupplierId, supplierId)
                .eq(warehouseId != null, PurchaseOrder::getWarehouseId, warehouseId)
                .eq(auditStatus != null, PurchaseOrder::getAuditStatus, auditStatus)
                .orderByDesc(PurchaseOrder::getOrderTime);
        List<PurchaseOrder> orders = list(wrapper);
        // 填充明细
        for (PurchaseOrder order : orders) {
            order.setDetails(detailMapper.selectByOrderId(order.getId()));
        }
        return orders;
    }
}
