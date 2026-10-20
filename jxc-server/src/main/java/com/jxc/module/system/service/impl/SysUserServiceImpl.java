package com.jxc.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.common.BusinessException;
import com.jxc.common.PageResult;
import com.jxc.module.system.entity.SysUser;
import com.jxc.module.system.mapper.SysUserMapper;
import com.jxc.module.system.service.SysUserService;
import com.jxc.utils.PasswordUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 系统用户服务实现
 *
 * @author jxc
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public PageResult<SysUser> page(Long page, Long size, String username, String realName, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), SysUser::getUsername, username)
                .like(StringUtils.hasText(realName), SysUser::getRealName, realName)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime);
        IPage<SysUser> p = new Page<>(page, size);
        IPage<SysUser> result = page(p, wrapper);
        // 清空密码
        List<SysUser> records = result.getRecords();
        records.forEach(u -> u.setPassword(null));
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public boolean addUser(SysUser user, String password) {
        // 校验用户名唯一
        SysUser exist = getByUsername(user.getUsername());
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(PasswordUtils.encode(password));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        return save(user);
    }

    @Override
    public boolean updateUser(SysUser user) {
        if (user.getUsername() != null) {
            SysUser exist = getByUsername(user.getUsername());
            if (exist != null && !exist.getId().equals(user.getId())) {
                throw new BusinessException("用户名已存在");
            }
        }
        // 不允许通过此接口改密码
        user.setPassword(null);
        return updateById(user);
    }

    @Override
    public boolean resetPassword(Long userId, String password) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(PasswordUtils.encode(password));
        return updateById(update);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!PasswordUtils.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(PasswordUtils.encode(newPassword));
        return updateById(update);
    }

    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return getOne(wrapper);
    }
}
