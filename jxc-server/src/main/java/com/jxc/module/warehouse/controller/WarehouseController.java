package com.jxc.module.warehouse.controller;

import com.jxc.common.PageResult;
import com.jxc.common.Result;
import com.jxc.module.warehouse.entity.Warehouse;
import com.jxc.module.warehouse.service.WarehouseService;
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

/**
 * 仓库控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<PageResult<Warehouse>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String warehouseName,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) Integer status) {
        return Result.success(warehouseService.page(page, size, warehouseName, warehouseCode, status));
    }

    /**
     * 查询全部启用仓库
     */
    @GetMapping("/all")
    public Result<List<Warehouse>> all() {
        return Result.success(warehouseService.listAllEnabled());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public Result<Warehouse> get(@PathVariable Long id) {
        return Result.success(warehouseService.getWarehouseById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Void> add(@RequestBody Warehouse warehouse) {
        warehouseService.save(warehouse);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping
    public Result<Void> update(@RequestBody Warehouse warehouse) {
        warehouseService.updateById(warehouse);
        return Result.success();
    }

    /**
     * 删除(校验是否存在库存记录)
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseService.deleteWithCheck(id);
        return Result.success();
    }
}
