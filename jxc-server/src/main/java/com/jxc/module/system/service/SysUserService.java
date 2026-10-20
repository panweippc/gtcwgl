package com.jxc.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.common.PageResult;
import com.jxc.module.system.entity.SysUser;

/**
 * 系统用户服务接口
 *
 * @author jxc
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户
     *
     * @param page     页码
     * @param size     每页大小
     * @param username 用户名
     * @param realName 真实姓名
     * @param status   状态
     * @return 分页结果
     */
    PageResult<SysUser> page(Long page, Long size, String username, String realName, Integer status);

    /**
     * 新增用户
     *
     * @param user     用户
     * @param password 原始密码
     * @return 是否成功
     */
    boolean addUser(SysUser user, String password);

    /**
     * 修改用户
     *
     * @param user 用户
     * @return 是否成功
     */
    boolean updateUser(SysUser user);

    /**
     * 重置密码
     *
     * @param userId   用户ID
     * @param password 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long userId, String password);

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 根据用户名查询
     *
     * @param username 用户名
     * @return 用户
     */
    SysUser getByUsername(String username);
}
