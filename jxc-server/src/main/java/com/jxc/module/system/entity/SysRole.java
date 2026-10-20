package com.jxc.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jxc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体
 *
 * @author jxc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 角色名称 */
    private String roleName;

    /** 角色编码 */
    private String roleCode;

    /** 数据范围:1-全部,2-本人 */
    private Integer dataScope;

    /** 排序 */
    private Integer sort;

    /** 状态:0-禁用,1-启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
