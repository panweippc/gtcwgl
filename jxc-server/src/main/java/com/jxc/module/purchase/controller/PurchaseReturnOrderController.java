package com.jxc.module.purchase.controller;

import com.jxc.common.PageResult;
import com.jxc.common.Result;
import com.jxc.module.purchase.entity.PurchaseReturnOrder;
import com.jxc.module.purchase.service.PurchaseReturnOrderService;
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
 * 采购退货控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/purchase/return")
@RequiredArgsConstructor
public class PurchaseReturnOrderController {

    private final PurchaseReturnOrderService returnService;

    /** 分页查询 */
    @GetMapping("/page")
    public Result<PageResult<PurchaseReturnOrder>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(returnService.page(page, size, orderNo, supplierId, warehouseId, auditStatus));
    }

    /** 导出全部匹配条件的采购退货单(含明细),供前端生成 Excel */
    @GetMapping("/export")
    public Result<List<PurchaseReturnOrder>> export(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(returnService.export(orderNo, supplierId, warehouseId, auditStatus));
    }

    /** 详情(含明细) */
    @GetMapping("/{id}")
    public Result<PurchaseReturnOrder> get(@PathVariable Long id) {
        return Result.success(returnService.getDetail(id));
    }

    /** 新增采购退货单 */
    @PostMapping
    public Result<Void> add(@RequestBody PurchaseReturnOrder order) {
        returnService.create(order);
        return Result.success();
    }

    /** 修改采购退货单(仅待审核可改) */
    @PutMapping
    public Result<Void> update(@RequestBody PurchaseReturnOrder order) {
        returnService.update(order);
        return Result.success();
    }

    /** 删除采购退货单(仅待审核可删) */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        returnService.delete(id);
        return Result.success();
    }

    /** 审核采购退货单(扣减库存) */
    @PostMapping("/audit/{id}")
    public Result<Void> audit(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        returnService.audit(id, remark);
        return Result.success();
    }
}
