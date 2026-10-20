package com.jxc.module.stock.controller;

import com.jxc.common.PageResult;
import com.jxc.common.Result;
import com.jxc.module.stock.entity.StockCheck;
import com.jxc.module.stock.service.StockCheckService;
import com.jxc.module.stock.vo.StockBookVO;
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
 * 库存盘点控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/stock/check")
@RequiredArgsConstructor
public class StockCheckController {

    private final StockCheckService checkService;

    /**
     * 分页查询盘点单
     */
    @GetMapping("/page")
    public Result<PageResult<StockCheck>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(checkService.page(page, size, orderNo, warehouseId, auditStatus));
    }

    /**
     * 导出全部匹配条件的盘点单(含明细),供前端生成 Excel
     */
    @GetMapping("/export")
    public Result<List<StockCheck>> export(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(checkService.export(orderNo, warehouseId, auditStatus));
    }

    /**
     * 查询指定仓库的账面库存列表(盘点自动带出)
     */
    @GetMapping("/book-list")
    public Result<List<StockBookVO>> bookList(@RequestParam Long warehouseId) {
        return Result.success(checkService.bookList(warehouseId));
    }

    /**
     * 详情(含明细)
     */
    @GetMapping("/{id}")
    public Result<StockCheck> get(@PathVariable Long id) {
        return Result.success(checkService.getDetail(id));
    }

    /**
     * 新增盘点单
     */
    @PostMapping
    public Result<Void> add(@RequestBody StockCheck check) {
        checkService.create(check);
        return Result.success();
    }

    /**
     * 修改盘点单(仅待盘点可改)
     */
    @PutMapping
    public Result<Void> update(@RequestBody StockCheck check) {
        checkService.update(check);
        return Result.success();
    }

    /**
     * 删除盘点单(仅待盘点可删)
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkService.delete(id);
        return Result.success();
    }

    /**
     * 执行盘点(生成盘盈/盘亏单,盘盈增加库存、盘亏扣减库存,盘点后不可改)
     */
    @PostMapping("/audit/{id}")
    public Result<Void> audit(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        checkService.audit(id, remark);
        return Result.success();
    }
}
