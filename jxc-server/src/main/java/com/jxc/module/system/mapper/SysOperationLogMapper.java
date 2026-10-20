package com.jxc.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxc.module.system.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 *
 * @author jxc
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {
}
