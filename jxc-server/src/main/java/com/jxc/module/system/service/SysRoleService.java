package com.jxc.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.common.PageResult;
import com.jxc.module.system.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author jxc
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 分页查询
     *
     * @param page     页码
     * @param size     每页大小
     * @param roleName 角色名
     * @return 分页
     */
    PageResult<SysRole> page(Long page, Long size, String roleName);

    /**
     * 查询全部启用角色
     *
     * @return 角色列表
     */
    List<SysRole> listAllEnabled();

    /**
     * 分配菜单给角色
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID集合
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 查询角色已分配的菜单ID集合
     *
     * @param roleId 角色ID
     * @return 菜单ID集合
     */
    List<Long> getMenuIds(Long roleId);

    /**
     * 根据ID查询角色
     *
     * @param id ID
     * @return 角色
     */
    SysRole getRoleById(Long id);
}
