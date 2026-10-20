package com.jxc.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxc.module.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper
 *
 * @author jxc
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户ID查询菜单ID列表
     *
     * @param userId 用户ID
     * @return 菜单ID列表
     */
    @Select("SELECT m.id FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON rm.menu_id = m.id " +
            "INNER JOIN sys_user u ON u.role_id = rm.role_id " +
            "WHERE u.id = #{userId} AND u.deleted = 0 AND m.deleted = 0 AND m.status = 1")
    List<Long> selectMenuIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
