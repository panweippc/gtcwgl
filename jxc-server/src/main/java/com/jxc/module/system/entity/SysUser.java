package com.jxc.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jxc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 系统用户实体
 *
 * @author jxc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    private String username;

    /** 密码 */
    @JsonIgnore
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 性别:1-男,2-女 */
    private Integer gender;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;

    /** 角色ID */
    private Long roleId;

    /** 状态:0-禁用,1-启用 */
    private Integer status;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    /** 备注 */
    private String remark;
}
