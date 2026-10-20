package com.jxc.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.module.system.entity.SysMenu;
import com.jxc.module.system.mapper.SysMenuMapper;
import com.jxc.module.system.mapper.SysRoleMapper;
import com.jxc.module.system.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 *
 * @author jxc
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysRoleMapper roleMapper;

    @Override
    public List<SysMenu> tree() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getStatus, 1).orderByAsc(SysMenu::getSort).orderByAsc(SysMenu::getId);
        List<SysMenu> all = list(wrapper);
        return buildTree(all);
    }

    @Override
    public List<SysMenu> treeByUserId(Long userId) {
        List<Long> menuIds = roleMapper.selectMenuIdsByUserId(userId);
        if (menuIds == null || menuIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<SysMenu> menus = baseMapper.selectMenusByIds(menuIds);
        return buildTree(menus);
    }

    @Override
    public List<String> permissions(Long userId) {
        List<Long> menuIds = roleMapper.selectMenuIdsByUserId(userId);
        if (menuIds == null || menuIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<SysMenu> menus = baseMapper.selectMenusByIds(menuIds);
        return menus.stream()
                .filter(m -> StringUtils.hasText(m.getPerms()))
                .map(SysMenu::getPerms)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 构建菜单树
     *
     * @param all 全部菜单
     * @return 树
     */
    private List<SysMenu> buildTree(List<SysMenu> all) {
        List<SysMenu> roots = all.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0L)
                .collect(Collectors.toList());
        roots.forEach(root -> fillChildren(root, all));
        roots.sort(Comparator.comparing(SysMenu::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
        return roots;
    }

    /**
     * 递归填充子菜单
     */
    private void fillChildren(SysMenu parent, List<SysMenu> all) {
        List<SysMenu> children = all.stream()
                .filter(m -> parent.getId().equals(m.getParentId()))
                .collect(Collectors.toList());
        children.forEach(c -> fillChildren(c, all));
        children.sort(Comparator.comparing(SysMenu::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
        parent.setChildren(children);
    }
}
