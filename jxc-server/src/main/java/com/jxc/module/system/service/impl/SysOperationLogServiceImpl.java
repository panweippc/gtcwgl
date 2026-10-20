package com.jxc.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.common.PageResult;
import com.jxc.module.system.entity.SysOperationLog;
import com.jxc.module.system.mapper.SysOperationLogMapper;
import com.jxc.module.system.service.SysOperationLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 操作日志服务实现
 *
 * @author jxc
 */
@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog> implements SysOperationLogService {

    @Override
    public PageResult<SysOperationLog> page(Long page, Long size, String module, String username, Boolean onlyError, String startDate, String endDate) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(module), SysOperationLog::getModule, module)
                .like(StringUtils.hasText(username), SysOperationLog::getUsername, username);
        // 只查询异常: status = 0
        if (Boolean.TRUE.equals(onlyError)) {
            wrapper.eq(SysOperationLog::getStatus, 0);
        }
        // 操作时间范围过滤
        if (StringUtils.hasText(startDate)) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            wrapper.ge(SysOperationLog::getCreateTime, start);
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            wrapper.le(SysOperationLog::getCreateTime, end);
        }
        wrapper.orderByDesc(SysOperationLog::getCreateTime);
        IPage<SysOperationLog> p = new Page<>(page, size);
        IPage<SysOperationLog> result = page(p, wrapper);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Async
    @Override
    public void saveAsync(SysOperationLog log) {
        save(log);
    }
}
