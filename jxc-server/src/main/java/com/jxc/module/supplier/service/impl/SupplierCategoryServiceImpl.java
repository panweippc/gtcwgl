package com.jxc.module.supplier.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.module.supplier.entity.SupplierCategory;
import com.jxc.module.supplier.mapper.SupplierCategoryMapper;
import com.jxc.module.supplier.service.SupplierCategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 供应商分类服务实现
 *
 * @author jxc
 */
@Service
public class SupplierCategoryServiceImpl extends ServiceImpl<SupplierCategoryMapper, SupplierCategory> implements SupplierCategoryService {

    @Override
    public List<SupplierCategory> tree() {
        // 下拉选择只显示启用分类
        List<SupplierCategory> all = listEnabledOrdered();
        return buildTree(all);
    }

    @Override
    public List<SupplierCategory> treeAll() {
        // 分类管理列表需显示全部分类(含禁用),便于查看和管理
        List<SupplierCategory> all = listAllOrdered();
        return buildTree(all);
    }

    @Override
    public List<SupplierCategory> listAllEnabled() {
        // 下拉选择只显示启用的分类
        return listEnabledOrdered();
    }

    /**
     * 查询全部分类(含禁用),按 sort、id 升序排序
     * 供 treeAll() 列表展示使用,禁用分类也需可见
     *
     * @return 全部分类列表
     */
    private List<SupplierCategory> listAllOrdered() {
        LambdaQueryWrapper<SupplierCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SupplierCategory::getSort)
                .orderByAsc(SupplierCategory::getId);
        return list(wrapper);
    }

    /**
     * 查询所有启用的分类,按 sort、id 升序排序
     * 供 tree() 和 listAllEnabled() 下拉选择使用
     *
     * @return 启用分类列表
     */
    private List<SupplierCategory> listEnabledOrdered() {
        LambdaQueryWrapper<SupplierCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplierCategory::getStatus, 1)
                .orderByAsc(SupplierCategory::getSort)
                .orderByAsc(SupplierCategory::getId);
        return list(wrapper);
    }

    /**
     * 构建分类树
     *
     * @param all 全部分类
     * @return 分类树
     */
    private List<SupplierCategory> buildTree(List<SupplierCategory> all) {
        List<SupplierCategory> roots = all.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0L)
                .collect(Collectors.toList());
        roots.forEach(root -> fillChildren(root, all));
        roots.sort(Comparator.comparing(SupplierCategory::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
        return roots;
    }

    /**
     * 递归填充子分类
     *
     * @param parent 父分类
     * @param all    全部分类
     */
    private void fillChildren(SupplierCategory parent, List<SupplierCategory> all) {
        List<SupplierCategory> children = all.stream()
                .filter(c -> parent.getId().equals(c.getParentId()))
                .collect(Collectors.toList());
        children.forEach(c -> fillChildren(c, all));
        children.sort(Comparator.comparing(SupplierCategory::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
        parent.setChildren(children);
    }
}
