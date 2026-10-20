package com.jxc.module.stock.controller;

import com.jxc.common.PageResult;
import com.jxc.common.Result;
import com.jxc.module.stock.entity.StockTransfer;
import com.jxc.module.stock.service.StockTransferService;
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
 * 库存调拨控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/stock/transfer")
@RequiredArgsConstructor
public class StockTransferController {

    private final StockTransferService transferService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<PageResult<StockTransfer>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long fromWarehouseId,
            @RequestParam(required = false) Long toWarehouseId,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(transferService.page(page, size, orderNo, fromWarehouseId, toWarehouseId, auditStatus));
    }

    /**
     * 导出全部匹配条件的调拨单(含明细),供前端生成 Excel
     */
    @GetMapping("/export")
    public Result<List<StockTransfer>> export(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long fromWarehouseId,
            @RequestParam(required = false) Long toWarehouseId,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(transferService.export(orderNo, fromWarehouseId, toWarehouseId, auditStatus));
    }

    /**
     * 详情(含明细)
     */
    @GetMapping("/{id}")
    public Result<StockTransfer> get(@PathVariable Long id) {
        return Result.success(transferService.getDetail(id));
    }

    /**
     * 新增调拨单
     */
    @PostMapping
    public Result<Void> add(@RequestBody StockTransfer transfer) {
        transferService.create(transfer);
        return Result.success();
    }

    /**
     * 修改调拨单(仅待审核可改)
     */
    @PutMapping
    public Result<Void> update(@RequestBody StockTransfer transfer) {
        transferService.update(transfer);
        return Result.success();
    }

    /**
     * 删除调拨单(仅待审核可删)
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        transferService.delete(id);
        return Result.success();
    }

    /**
     * 审核调拨单(执行库存调拨,审核后不可改)
     */
    @PostMapping("/audit/{id}")
    public Result<Void> audit(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        transferService.audit(id, remark);
        return Result.success();
    }
}
