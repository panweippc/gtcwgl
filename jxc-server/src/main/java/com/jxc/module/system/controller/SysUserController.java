package com.jxc.module.system.controller;

import com.jxc.common.BusinessException;
import com.jxc.common.PageResult;
import com.jxc.common.Result;
import com.jxc.context.UserContext;
import com.jxc.module.system.entity.SysUser;
import com.jxc.module.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统用户控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status) {
        return Result.success(userService.page(page, size, username, realName, status));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public Result<SysUser> get(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Void> add(@RequestBody SysUser user,
                            @RequestParam(defaultValue = "123456") String password) {
        userService.addUser(user, password);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        userService.updateUser(user);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String password = body.get("password").toString();
        userService.resetPassword(userId, password);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        Long userId = com.jxc.context.UserContext.getCurrentUserId();
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }

    /**
     * 切换状态
     */
    @PostMapping("/status/{id}")
    public Result<Void> status(@PathVariable Long id, @RequestParam Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 获取当前登录用户详情(个人信息页用)
     */
    @GetMapping("/profile")
    public Result<SysUser> profile() {
        Long userId = UserContext.getCurrentUserId();
        SysUser user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    /**
     * 更新当前登录用户个人信息(只允许改 realName/gender/avatar/phone/email/remark)
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody SysUser user) {
        Long userId = UserContext.getCurrentUserId();
        SysUser exist = userService.getById(userId);
        if (exist == null) {
            throw new BusinessException("用户不存在");
        }
        exist.setRealName(user.getRealName());
        exist.setGender(user.getGender());
        exist.setAvatar(user.getAvatar());
        exist.setPhone(user.getPhone());
        exist.setEmail(user.getEmail());
        exist.setRemark(user.getRemark());
        userService.updateById(exist);
        return Result.success();
    }
}
