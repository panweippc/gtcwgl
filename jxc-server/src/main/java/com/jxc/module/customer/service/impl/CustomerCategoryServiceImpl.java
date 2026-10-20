package com.jxc.module.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.module.customer.entity.CustomerCategory;
import com.jxc.module.customer.mapper.CustomerCategoryMapper;
import com.jxc.module.customer.service.CustomerCategoryService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 客户分类服务实现
 * 提供树形查询与启用列表查询,支持循环引用防护
 *
 * @author jxc
 */
@Service
public class CustomerCategoryServiceImpl extends ServiceImpl<CustomerCategoryMapper, CustomerCategory> implements CustomerCategoryService {

    /**
     * 根节点父ID常量
     */
    private static final Long ROOT_PARENT_ID = Long.valueOf(0L);

    @Override
    public List<CustomerCategory> tree() {
        // 下拉选择只显示启用分类
        List<CustomerCategory> all = listEnabledOrdered();
        return buildTree(all);
    }

    @Override
    public List<CustomerCategory> treeAll() {
        // 分类管理列表需显示全部分类(含禁用),便于查看和管理
        List<CustomerCategory> all = listAllOrdered();
        return buildTree(all);
    }

    @Override
    public List<CustomerCategory> listAllEnabled() {
        // 下拉选择只显示启用的分类
        return listEnabledOrdered();
    }

    /**
     * 查询全部分类(含禁用),按 sort、id 升序排序
     * 供 tree() 列表展示使用,禁用分类也需可见
     *
     * @return 全部分类列表
     */
    private List<CustomerCategory> listAllOrdered() {
        LambdaQueryWrapper<CustomerCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(CustomerCategory::getSort)
                .orderByAsc(CustomerCategory::getId);
        return list(wrapper);
    }

    /**
     * 查询所有启用的分类,按 sort、id 升序排序
     * 供 listAllEnabled() 下拉选择使用,只显示启用的分类
     *
     * @return 启用分类列表
     */
    private List<CustomerCategory> listEnabledOrdered() {
        LambdaQueryWrapper<CustomerCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerCategory::getStatus, 1)
                .orderByAsc(CustomerCategory::getSort)
                .orderByAsc(CustomerCategory::getId);
        return list(wrapper);
    }

    /**
     * 构建分类树
     * 使用访问集合防止循环引用导致无限递归
     *
     * @param all 全部分类
     * @return 分类树
     */
    private static List<CustomerCategory> buildTree(List<CustomerCategory> all) {
        List<CustomerCategory> roots = all.stream()
                .filter(CustomerCategoryServiceImpl::isRoot)
                .collect(Collectors.toList());
        // 按 sort 升序排序(null 排在最后),确保列表展示顺序正确
        roots.sort(Comparator.comparing(CustomerCategory::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
        Set<Long> visited = new HashSet<>(all.size());
        roots.forEach(root -> fillChildren(root, all, visited));
        return roots;
    }

    /**
     * 判断是否为根节点
     *
     * @param category 分类
     * @return true-根节点
     */
    private static boolean isRoot(CustomerCategory category) {
        Long parentId = category.getParentId();
        return parentId == null || Objects.equals(parentId, ROOT_PARENT_ID);
    }

    /**
     * 递归填充子分类
     * 使用 visited 集合记录已处理的节点,防止循环引用导致 StackOverflow
     *
     * @param parent  父分类
     * @param all     全部分类
     * @param visited 已处理的分类ID集合
     */
    private static void fillChildren(CustomerCategory parent, List<CustomerCategory> all, Set<Long> visited) {
        if (parent.getId() == null || !visited.add(parent.getId())) {
            // 已访问过或ID为空,出现循环引用,停止递归
            parent.setChildren(Collections.emptyList());
            return;
        }
        List<CustomerCategory> children = all.stream()
                .filter(c -> parent.getId().equals(c.getParentId()))
                .collect(Collectors.toList());
        // 子分类也按 sort 升序排序(null 排在最后)
        children.sort(Comparator.comparing(CustomerCategory::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
        children.forEach(c -> fillChildren(c, all, visited));
        parent.setChildren(children);
    }
}
