package com.jxc.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jxc.common.BusinessException;
import com.jxc.common.Result;
import com.jxc.module.product.entity.Product;
import com.jxc.module.product.entity.ProductCategory;
import com.jxc.module.product.mapper.ProductMapper;
import com.jxc.module.product.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品分类控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/product/category")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;
    private final ProductMapper productMapper;

    /**
     * 分类树(仅启用),供下拉选择使用
     */
    @GetMapping("/tree")
    public Result<List<ProductCategory>> tree() {
        return Result.success(categoryService.tree());
    }

    /**
     * 全部分类树(含禁用),供分类管理列表展示使用
     */
    @GetMapping("/treeAll")
    public Result<List<ProductCategory>> treeAll() {
        return Result.success(categoryService.treeAll());
    }

    /**
     * 查询全部启用分类
     */
    @GetMapping("/all")
    public Result<List<ProductCategory>> all() {
        return Result.success(categoryService.listAllEnabled());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public Result<ProductCategory> get(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Void> add(@RequestBody ProductCategory category) {
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        categoryService.save(category);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping
    public Result<Void> update(@RequestBody ProductCategory category) {
        categoryService.updateById(category);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 1. 检查是否存在子分类
        Long childCount = categoryService.count(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("该分类存在子分类,不可删除");
        }
        // 2. 检查是否被商品引用
        Long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getCategoryId, id));
        if (productCount > 0) {
            throw new BusinessException("该分类已被商品引用,不可删除");
        }
        categoryService.removeById(id);
        return Result.success();
    }
}
