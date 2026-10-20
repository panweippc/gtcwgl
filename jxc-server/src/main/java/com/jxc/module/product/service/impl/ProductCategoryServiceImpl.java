package com.jxc.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.module.product.entity.ProductCategory;
import com.jxc.module.product.mapper.ProductCategoryMapper;
import com.jxc.module.product.service.ProductCategoryService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现
 *
 * @author jxc
 */
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    @Override
    public List<ProductCategory> tree() {
        // 下拉选择只显示启用分类
        List<ProductCategory> all = listEnabledOrdered();
        return buildTree(all);
    }

    @Override
    public List<ProductCategory> treeAll() {
        // 分类管理列表需显示全部分类(含禁用),便于查看和管理
        List<ProductCategory> all = listAllOrdered();
        return buildTree(all);
    }

    @Override
    public List<ProductCategory> listAllEnabled() {
        // 下拉选择只显示启用的分类
        return listEnabledOrdered();
    }

    /**
     * 查询全部分类(含禁用),按 sort、id 升序排序
     * 供 treeAll() 列表展示使用,禁用分类也需可见
     *
     * @return 全部分类列表
     */
    private List<ProductCategory> listAllOrdered() {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ProductCategory::getSort)
                .orderByAsc(ProductCategory::getId);
        return list(wrapper);
    }

    /**
     * 查询所有启用的分类,按 sort、id 升序排序
     * 供 tree() 和 listAllEnabled() 下拉选择使用
     *
     * @return 启用分类列表
     */
    private List<ProductCategory> listEnabledOrdered() {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSort)
                .orderByAsc(ProductCategory::getId);
        return list(wrapper);
    }

    /**
     * 构建分类树
     *
     * @param all 全部分类
     * @return 分类树
     */
    private List<ProductCategory> buildTree(List<ProductCategory> all) {
        List<ProductCategory> roots = all.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0L)
                .collect(Collectors.toList());
        roots.forEach(root -> fillChildren(root, all));
        roots.sort(Comparator.comparing(ProductCategory::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
        return roots;
    }

    /**
     * 递归填充子分类
     *
     * @param parent 父分类
     * @param all    全部分类
     */
    private void fillChildren(ProductCategory parent, List<ProductCategory> all) {
        List<ProductCategory> children = all.stream()
                .filter(c -> parent.getId().equals(c.getParentId()))
                .collect(Collectors.toList());
        children.forEach(c -> fillChildren(c, all));
        children.sort(Comparator.comparing(ProductCategory::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
        parent.setChildren(children);
    }
}
