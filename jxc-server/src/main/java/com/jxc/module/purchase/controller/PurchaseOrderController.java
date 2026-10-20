package com.jxc.module.purchase.controller;

import com.jxc.common.PageResult;
import com.jxc.common.Result;
import com.jxc.module.purchase.entity.PurchaseOrder;
import com.jxc.module.purchase.service.PurchaseOrderService;
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
 * 采购入库控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/purchase/order")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService orderService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<PageResult<PurchaseOrder>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(orderService.page(page, size, orderNo, supplierId, warehouseId, auditStatus));
    }

    /**
     * 导出全部匹配条件的采购单(含明细),供前端生成 Excel
     */
    @GetMapping("/export")
    public Result<List<PurchaseOrder>> export(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(orderService.export(orderNo, supplierId, warehouseId, auditStatus));
    }

    /**
     * 详情(含明细)
     */
    @GetMapping("/{id}")
    public Result<PurchaseOrder> get(@PathVariable Long id) {
        return Result.success(orderService.getDetail(id));
    }

    /**
     * 新增采购单
     */
    @PostMapping
    public Result<Void> add(@RequestBody PurchaseOrder order) {
        orderService.create(order);
        return Result.success();
    }

    /**
     * 修改采购单(仅待审核可改)
     */
    @PutMapping
    public Result<Void> update(@RequestBody PurchaseOrder order) {
        orderService.update(order);
        return Result.success();
    }

    /**
     * 删除采购单(仅待审核可删)
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return Result.success();
    }

    /**
     * 审核采购单(增加库存,审核后不可改)
     */
    @PostMapping("/audit/{id}")
    public Result<Void> audit(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        orderService.audit(id, remark);
        return Result.success();
    }
}
