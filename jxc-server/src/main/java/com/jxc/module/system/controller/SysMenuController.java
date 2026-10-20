package com.jxc.module.system.controller;

import com.jxc.common.Result;
import com.jxc.module.system.entity.SysMenu;
import com.jxc.module.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    /**
     * 菜单树
     */
    @GetMapping("/tree")
    public Result<List<SysMenu>> tree() {
        return Result.success(menuService.tree());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public Result<SysMenu> get(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Void> add(@RequestBody SysMenu menu) {
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        menuService.save(menu);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping
    public Result<Void> update(@RequestBody SysMenu menu) {
        menuService.updateById(menu);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.removeById(id);
        return Result.success();
    }
}
