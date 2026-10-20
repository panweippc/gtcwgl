package com.jxc.module.system.controller;

import com.jxc.common.PageResult;
import com.jxc.common.Result;
import com.jxc.module.system.entity.SysRole;
import com.jxc.module.system.service.SysRoleService;
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

import java.util.List;
import java.util.Map;

/**
 * 角色控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<PageResult<SysRole>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String roleName) {
        return Result.success(roleService.page(page, size, roleName));
    }

    /**
     * 查询全部启用角色
     */
    @GetMapping("/all")
    public Result<List<SysRole>> all() {
        return Result.success(roleService.listAllEnabled());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public Result<SysRole> get(@PathVariable Long id) {
        return Result.success(roleService.getRoleById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Void> add(@RequestBody SysRole role) {
        roleService.save(role);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping
    public Result<Void> update(@RequestBody SysRole role) {
        roleService.updateById(role);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return Result.success();
    }

    /**
     * 分配菜单
     */
    @PostMapping("/assign-menus")
    public Result<Void> assignMenus(@RequestBody Map<String, Object> body) {
        Long roleId = Long.valueOf(body.get("roleId").toString());
        @SuppressWarnings("unchecked")
        List<Long> menuIds = ((List<Object>) body.get("menuIds"))
                .stream().map(o -> Long.valueOf(o.toString())).collect(java.util.stream.Collectors.toList());
        roleService.assignMenus(roleId, menuIds);
        return Result.success();
    }

    /**
     * 查询角色菜单ID集合
     */
    @GetMapping("/menus/{roleId}")
    public Result<List<Long>> roleMenus(@PathVariable Long roleId) {
        return Result.success(roleService.getMenuIds(roleId));
    }
}
