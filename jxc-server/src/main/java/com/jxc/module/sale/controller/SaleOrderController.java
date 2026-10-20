package com.jxc.module.sale.controller;

import com.jxc.common.PageResult;
import com.jxc.common.Result;
import com.jxc.module.sale.entity.SaleOrder;
import com.jxc.module.sale.service.SaleOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 销售出库控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/sale/order")
@RequiredArgsConstructor
public class SaleOrderController {

    private final SaleOrderService orderService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<PageResult<SaleOrder>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(orderService.page(page, size, orderNo, customerId, warehouseId, auditStatus));
    }

    /**
     * 详情(含明细)
     */
    @GetMapping("/{id}")
    public Result<SaleOrder> get(@PathVariable Long id) {
        return Result.success(orderService.getDetail(id));
    }

    /**
     * 新增销售单
     */
    @PostMapping
    public Result<Void> add(@RequestBody SaleOrder order) {
        orderService.create(order);
        return Result.success();
    }

    /**
     * 修改销售单(仅待审核可改)
     */
    @PutMapping
    public Result<Void> update(@RequestBody SaleOrder order) {
        orderService.update(order);
        return Result.success();
    }

    /**
     * 删除销售单(仅待审核可删)
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return Result.success();
    }

    /**
     * 审核销售单(扣减库存,审核后不可改)
     */
    @PostMapping("/audit/{id}")
    public Result<Void> audit(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        orderService.audit(id, remark);
        return Result.success();
    }

    /** 导出全部匹配条件的销售出库单(含明细),供前端生成 Excel */
    @GetMapping("/export")
    public Result<List<SaleOrder>> export(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(orderService.export(orderNo, customerId, warehouseId, auditStatus));
    }
}
