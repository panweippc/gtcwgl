package com.jxc.module.system.vo;

import com.jxc.module.system.entity.SysMenu;
import lombok.Data;

import java.util.List;

/**
 * 用户信息(登录后返回)
 *
 * @author jxc
 */
@Data
public class UserInfoVO {

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 头像 */
    private String avatar;

    /** 角色ID */
    private Long roleId;

    /** 角色名称 */
    private String roleName;

    /** 角色编码 */
    private String roleCode;

    /** 权限标识列表 */
    private List<String> permissions;

    /** 菜单树 */
    private List<SysMenu> menus;
}
