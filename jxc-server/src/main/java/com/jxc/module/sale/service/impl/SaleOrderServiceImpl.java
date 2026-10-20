package com.jxc.module.sale.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.common.BusinessException;
import com.jxc.common.PageResult;
import com.jxc.common.ResultCode;
import com.jxc.context.UserContext;
import com.jxc.module.sale.entity.SaleOrder;
import com.jxc.module.sale.entity.SaleOrderDetail;
import com.jxc.module.sale.mapper.SaleOrderDetailMapper;
import com.jxc.module.sale.mapper.SaleOrderMapper;
import com.jxc.module.sale.service.SaleOrderService;
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
 * 销售出库服务实现
 * 规则:审核后单据不可改、审核时扣减库存、出库事务控制、库存不足禁止负库存
 *
 * @author jxc
 */
@Service
@RequiredArgsConstructor
public class SaleOrderServiceImpl extends ServiceImpl<SaleOrderMapper, SaleOrder> implements SaleOrderService {

    private final SaleOrderDetailMapper detailMapper;
    private final StockService stockService;

    @Override
    public PageResult<SaleOrder> page(Long page, Long size, String orderNo,
                                      Long customerId, Long warehouseId, Integer auditStatus) {
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), SaleOrder::getOrderNo, orderNo)
                .eq(customerId != null, SaleOrder::getCustomerId, customerId)
                .eq(warehouseId != null, SaleOrder::getWarehouseId, warehouseId)
                .eq(auditStatus != null, SaleOrder::getAuditStatus, auditStatus)
                .orderByDesc(SaleOrder::getCreateTime);
        IPage<SaleOrder> p = new Page<>(page, size);
        IPage<SaleOrder> result = page(p, wrapper);
        // 批量填充明细,供列表展示商品
        fillDetails(result.getRecords());
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    /**
     * 批量填充明细(IN 查询一次取回所有明细)
     */
    private void fillDetails(List<SaleOrder> orders) {
        if (CollectionUtils.isEmpty(orders)) return;
        List<Long> orderIds = orders.stream().map(SaleOrder::getId).collect(Collectors.toList());
        List<SaleOrderDetail> allDetails = detailMapper.selectList(
                new LambdaQueryWrapper<SaleOrderDetail>().in(SaleOrderDetail::getOrderId, orderIds));
        Map<Long, List<SaleOrderDetail>> groupBy = allDetails.stream()
                .collect(Collectors.groupingBy(SaleOrderDetail::getOrderId));
        for (SaleOrder o : orders) {
            o.setDetails(groupBy.getOrDefault(o.getId(), Collections.emptyList()));
        }
    }

    @Override
    public SaleOrder getDetail(Long id) {
        SaleOrder order = getById(id);
        if (order == null) {
            return null;
        }
        List<SaleOrderDetail> details = detailMapper.selectByOrderId(id);
        order.setDetails(details);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(SaleOrder order) {
        if (CollectionUtils.isEmpty(order.getDetails())) {
            throw new BusinessException("销售明细不能为空");
        }
        order.setOrderNo(OrderNoGenerator.next("SO"));
        if (order.getOrderTime() == null) {
            order.setOrderTime(new Date());
        }
        order.setAuditStatus(0);
        // 计算合计
        calcTotal(order);
        save(order);
        for (SaleOrderDetail d : order.getDetails()) {
            d.setOrderId(order.getId());
            d.setOrderNo(order.getOrderNo());
            d.setCreateTime(new Date());
            detailMapper.insert(d);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SaleOrder order) {
        SaleOrder exist = getById(order.getId());
        if (exist == null) {
            throw new BusinessException("销售单不存在");
        }
        if (exist.getAuditStatus() != null && exist.getAuditStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_AUDITED);
        }
        order.setOrderNo(exist.getOrderNo());
        order.setAuditStatus(0);
        calcTotal(order);
        updateById(order);
        if (!CollectionUtils.isEmpty(order.getDetails())) {
            detailMapper.delete(new LambdaQueryWrapper<SaleOrderDetail>()
                    .eq(SaleOrderDetail::getOrderId, order.getId()));
            for (SaleOrderDetail d : order.getDetails()) {
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
        SaleOrder exist = getById(id);
        if (exist == null) {
            throw new BusinessException("销售单不存在");
        }
        if (exist.getAuditStatus() != null && exist.getAuditStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_AUDITED);
        }
        detailMapper.delete(new LambdaQueryWrapper<SaleOrderDetail>()
                .eq(SaleOrderDetail::getOrderId, id));
        removeById(id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, String remark) {
        SaleOrder exist = getById(id);
        if (exist == null) {
            throw new BusinessException("销售单不存在");
        }
        if (exist.getAuditStatus() != null && exist.getAuditStatus() == 1) {
            throw new BusinessException("销售单已审核,请勿重复审核");
        }
        List<SaleOrderDetail> details = detailMapper.selectByOrderId(id);
        if (CollectionUtils.isEmpty(details)) {
            throw new BusinessException("销售明细为空,无法审核");
        }
        // 逐条扣减库存(库存不足抛 BusinessException,任一失败整体回滚,禁止负库存)
        for (SaleOrderDetail d : details) {
            if (d.getQuantity() == null || d.getQuantity() <= 0) {
                throw new BusinessException("销售数量必须大于0");
            }
            stockService.deductStock(exist.getWarehouseId(), d.getProductId(), d.getQuantity());
        }
        // 更新审核状态
        SaleOrder update = new SaleOrder();
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
     * @param order 销售单
     */
    private void calcTotal(SaleOrder order) {
        if (CollectionUtils.isEmpty(order.getDetails())) {
            return;
        }
        int totalQty = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SaleOrderDetail d : order.getDetails()) {
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
    public List<SaleOrder> export(String orderNo, Long customerId, Long warehouseId, Integer auditStatus) {
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), SaleOrder::getOrderNo, orderNo)
                .eq(customerId != null, SaleOrder::getCustomerId, customerId)
                .eq(warehouseId != null, SaleOrder::getWarehouseId, warehouseId)
                .eq(auditStatus != null, SaleOrder::getAuditStatus, auditStatus)
                .orderByDesc(SaleOrder::getOrderTime);
        List<SaleOrder> orders = list(wrapper);
        for (SaleOrder order : orders) {
            order.setDetails(detailMapper.selectByOrderId(order.getId()));
        }
        return orders;
    }
}
