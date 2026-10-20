package com.jxc.module.system.vo;

import lombok.Data;

/**
 * 登录返回结果
 *
 * @author jxc
 */
@Data
public class LoginVO {

    /** token */
    private String token;

    /** token 类型 */
    private String tokenType;

    /** 过期时间(秒) */
    private Long expireIn;

    /** 用户信息 */
    private UserInfoVO userInfo;
}
