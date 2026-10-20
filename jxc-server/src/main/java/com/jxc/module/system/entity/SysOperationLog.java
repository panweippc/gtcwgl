package com.jxc.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 操作日志实体
 *
 * @author jxc
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模块 */
    private String module;

    /** 操作内容 */
    private String operation;

    /** 请求方法 */
    private String method;

    /** 请求URL */
    private String requestUrl;

    /** 请求参数 */
    private String requestParam;

    /** HTTP方法 */
    private String requestMethod;

    /** 操作人ID */
    private Long userId;

    /** 操作人用户名 */
    private String username;

    /** IP地址 */
    private String ip;

    /** 耗时(毫秒) */
    private Long costTime;

    /** 状态:0-失败,1-成功 */
    private Integer status;

    /** 错误信息 */
    private String errorMsg;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
