package com.jxc.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.common.PageResult;
import com.jxc.module.system.entity.SysOperationLog;

/**
 * 操作日志服务接口
 *
 * @author jxc
 */
public interface SysOperationLogService extends IService<SysOperationLog> {

    /**
     * 分页查询
     *
     * @param page      页码
     * @param size      每页大小
     * @param module    操作菜单
     * @param username  用户名
     * @param onlyError 是否只查询异常(status=0)
     * @param startDate 操作开始日期(yyyy-MM-dd)
     * @param endDate   操作结束日期(yyyy-MM-dd)
     * @return 分页
     */
    PageResult<SysOperationLog> page(Long page, Long size, String module, String username, Boolean onlyError, String startDate, String endDate);

    /**
     * 异步保存日志
     *
     * @param log 日志
     */
    void saveAsync(SysOperationLog log);
}
