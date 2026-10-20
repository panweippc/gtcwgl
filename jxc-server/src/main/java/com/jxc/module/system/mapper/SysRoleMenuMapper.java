package com.jxc.module.system.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色菜单关联 Mapper
 *
 * @author jxc
 */
@Mapper
public interface SysRoleMenuMapper {

    /**
     * 删除角色的全部菜单关联
     *
     * @param roleId 角色ID
     * @return 受影响行数
     */
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 新增角色菜单关联
     *
     * @param roleId  角色ID
     * @param menuId  菜单ID
     * @return 受影响行数
     */
    @Insert("INSERT INTO sys_role_menu(role_id, menu_id) VALUES(#{roleId}, #{menuId})")
    int insert(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
}
