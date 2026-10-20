package com.jxc.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.module.system.entity.SysMenu;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author jxc
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 查询全部启用菜单(树结构)
     *
     * @return 菜单树
     */
    List<SysMenu> tree();

    /**
     * 根据用户ID查询菜单树
     *
     * @param userId 用户ID
     * @return 菜单树
     */
    List<SysMenu> treeByUserId(Long userId);

    /**
     * 查询用户拥有的权限标识集合
     *
     * @param userId 用户ID
     * @return 权限集合
     */
    List<String> permissions(Long userId);
}
