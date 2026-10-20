package com.jxc.module.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.module.customer.entity.CustomerCategory;

import java.util.List;

/**
 * 客户分类服务接口
 *
 * @author jxc
 */
public interface CustomerCategoryService extends IService<CustomerCategory> {

    /**
     * 分类树(仅启用),供下拉选择使用
     *
     * @return 启用分类树
     */
    List<CustomerCategory> tree();

    /**
     * 全部分类树(含禁用),供分类管理列表展示使用
     *
     * @return 全部分类树
     */
    List<CustomerCategory> treeAll();

    /**
     * 查询全部启用分类(平铺列表)
     *
     * @return 分类列表
     */
    List<CustomerCategory> listAllEnabled();
}
