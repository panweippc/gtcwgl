package com.jxc.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jxc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 菜单实体
 *
 * @author jxc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 父菜单ID */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 类型:1-目录,2-菜单,3-按钮 */
    private Integer menuType;

    /** 路由路径 */
    private String path;

    /** 前端组件 */
    private String component;

    /** 权限标识 */
    private String perms;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sort;

    /** 是否可见:0-隐藏,1-显示 */
    private Integer visible;

    /** 状态:0-禁用,1-启用 */
    private Integer status;

    /** 子菜单(非数据库字段) */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private List<SysMenu> children;
}
