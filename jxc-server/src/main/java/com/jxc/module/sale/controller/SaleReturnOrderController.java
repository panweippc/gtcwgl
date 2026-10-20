package com.jxc.module.sale.controller;

import com.jxc.common.PageResult;
import com.jxc.common.Result;
import com.jxc.module.sale.entity.SaleReturnOrder;
import com.jxc.module.sale.service.SaleReturnOrderService;
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
 * 销售退货控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/sale/return")
@RequiredArgsConstructor
public class SaleReturnOrderController {

    private final SaleReturnOrderService returnService;

    /** 分页查询 */
    @GetMapping("/page")
    public Result<PageResult<SaleReturnOrder>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(returnService.page(page, size, orderNo, customerId, warehouseId, auditStatus));
    }

    /** 详情(含明细) */
    @GetMapping("/{id}")
    public Result<SaleReturnOrder> get(@PathVariable Long id) {
        return Result.success(returnService.getDetail(id));
    }

    /** 新增销售退货单 */
    @PostMapping
    public Result<Void> add(@RequestBody SaleReturnOrder order) {
        returnService.create(order);
        return Result.success();
    }

    /** 修改销售退货单(仅待审核可改) */
    @PutMapping
    public Result<Void> update(@RequestBody SaleReturnOrder order) {
        returnService.update(order);
        return Result.success();
    }

    /** 删除销售退货单(仅待审核可删) */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        returnService.delete(id);
        return Result.success();
    }

    /** 审核销售退货单(增加库存) */
    @PostMapping("/audit/{id}")
    public Result<Void> audit(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        returnService.audit(id, remark);
        return Result.success();
    }

    /** 导出全部匹配条件的销售退货单(含明细),供前端生成 Excel */
    @GetMapping("/export")
    public Result<List<SaleReturnOrder>> export(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(returnService.export(orderNo, customerId, warehouseId, auditStatus));
    }
}
