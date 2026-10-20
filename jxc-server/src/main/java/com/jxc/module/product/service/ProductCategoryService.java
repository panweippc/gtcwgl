package com.jxc.module.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.module.product.entity.ProductCategory;

import java.util.List;

/**
 * 商品分类服务接口
 *
 * @author jxc
 */
public interface ProductCategoryService extends IService<ProductCategory> {

    /**
     * 分类树(仅启用),供下拉选择使用
     *
     * @return 启用分类树
     */
    List<ProductCategory> tree();

    /**
     * 全部分类树(含禁用),供分类管理列表展示使用
     *
     * @return 全部分类树
     */
    List<ProductCategory> treeAll();

    /**
     * 查询全部启用分类(平铺列表)
     *
     * @return 分类列表
     */
    List<ProductCategory> listAllEnabled();
}
