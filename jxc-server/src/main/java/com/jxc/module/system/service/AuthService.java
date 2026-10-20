package com.jxc.module.system.service;

import com.jxc.module.system.dto.LoginDTO;
import com.jxc.module.system.vo.LoginVO;
import com.jxc.module.system.vo.UserInfoVO;

/**
 * 认证服务接口
 *
 * @author jxc
 */
public interface AuthService {

    /**
     * 登录
     *
     * @param dto 登录请求
     * @return 登录结果
     */
    LoginVO login(LoginDTO dto);

    /**
     * 获取当前登录用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoVO getUserInfo(Long userId);
}
