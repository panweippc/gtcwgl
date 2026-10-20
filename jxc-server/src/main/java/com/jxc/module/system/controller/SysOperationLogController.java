package com.jxc.module.system.controller;

import com.jxc.common.PageResult;
import com.jxc.common.Result;
import com.jxc.module.system.entity.SysOperationLog;
import com.jxc.module.system.service.SysOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/system/log")
@RequiredArgsConstructor
public class SysOperationLogController {

    private final SysOperationLogService logService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<PageResult<SysOperationLog>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Boolean onlyError,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(logService.page(page, size, module, username, onlyError, startDate, endDate));
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        logService.removeById(id);
        return Result.success();
    }

    /**
     * 清空日志
     */
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        logService.remove(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
        return Result.success();
    }
}
