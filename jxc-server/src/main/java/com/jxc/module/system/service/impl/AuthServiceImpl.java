package com.jxc.module.system.service.impl;

import com.jxc.common.BusinessException;
import com.jxc.module.system.dto.LoginDTO;
import com.jxc.module.system.entity.SysMenu;
import com.jxc.module.system.entity.SysRole;
import com.jxc.module.system.entity.SysUser;
import com.jxc.module.system.service.AuthService;
import com.jxc.module.system.service.SysMenuService;
import com.jxc.module.system.service.SysRoleService;
import com.jxc.module.system.service.SysUserService;
import com.jxc.module.system.vo.LoginVO;
import com.jxc.module.system.vo.UserInfoVO;
import com.jxc.utils.JwtUtils;
import com.jxc.utils.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 认证服务实现
 *
 * @author jxc
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysMenuService menuService;
    private final JwtUtils jwtUtils;

    @Value("${jxc.jwt.expire}")
    private long expireMinutes;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(LoginDTO dto) {
        SysUser user = userService.getByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码不正确");
        }
        if (!PasswordUtils.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码不正确");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用,请联系管理员");
        }
        // 更新最后登录时间
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setLastLoginTime(new Date());
        userService.updateById(update);

        // 生成 token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setTokenType("Bearer");
        vo.setExpireIn(expireMinutes * 60L);
        vo.setUserInfo(getUserInfo(user.getId()));
        return vo;
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setRoleId(user.getRoleId());
        if (user.getRoleId() != null) {
            SysRole role = roleService.getRoleById(user.getRoleId());
            if (role != null) {
                vo.setRoleName(role.getRoleName());
                vo.setRoleCode(role.getRoleCode());
            }
        }
        // 权限标识
        List<String> perms = menuService.permissions(userId);
        vo.setPermissions(perms);
        // 菜单树
        List<SysMenu> menus = menuService.treeByUserId(userId);
        vo.setMenus(menus);
        return vo;
    }
}
