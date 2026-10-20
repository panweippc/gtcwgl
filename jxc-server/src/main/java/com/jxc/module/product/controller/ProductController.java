package com.jxc.module.product.controller;

import com.jxc.common.BusinessException;
import com.jxc.common.PageResult;
import com.jxc.common.Result;
import com.jxc.module.product.entity.Product;
import com.jxc.module.product.service.ProductService;
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
 * 商品控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 分页查询商品(多条件)
     */
    @GetMapping("/page")
    public Result<PageResult<Product>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return Result.success(productService.page(page, size, productName, productCode, categoryId, status));
    }

    /**
     * 查询全部启用商品
     */
    @GetMapping("/all")
    public Result<List<Product>> all() {
        return Result.success(productService.listAllEnabled());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public Result<Product> get(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Void> add(@RequestBody Product product) {
        productService.save(product);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping
    public Result<Void> update(@RequestBody Product product) {
        productService.updateById(product);
        return Result.success();
    }

    /**
     * 删除(校验是否存在采购/销售/退货/库存关联)
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteWithCheck(id);
        return Result.success();
    }
}
