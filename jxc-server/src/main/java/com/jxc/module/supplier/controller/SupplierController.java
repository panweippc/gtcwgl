package com.jxc.module.supplier.controller;

import com.jxc.common.PageResult;
import com.jxc.common.Result;
import com.jxc.module.supplier.entity.Supplier;
import com.jxc.module.supplier.service.SupplierService;
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
 * 供应商控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<PageResult<Supplier>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return Result.success(supplierService.page(page, size, supplierName, supplierCode, categoryId, status));
    }

    /**
     * 查询全部启用供应商
     */
    @GetMapping("/all")
    public Result<List<Supplier>> all() {
        return Result.success(supplierService.listAllEnabled());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public Result<Supplier> get(@PathVariable Long id) {
        return Result.success(supplierService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Void> add(@RequestBody Supplier supplier) {
        supplierService.save(supplier);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping
    public Result<Void> update(@RequestBody Supplier supplier) {
        supplierService.updateById(supplier);
        return Result.success();
    }

    /**
     * 删除(校验是否存在采购关联记录)
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.deleteWithCheck(id);
        return Result.success();
    }
}
