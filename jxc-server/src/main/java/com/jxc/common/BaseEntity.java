package com.jxc.common;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 实体基类
 * 公共字段:id、创建时间、更新时间、逻辑删除标记
 *
 * @author jxc
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableField("id")
    private Long id;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /** 创建人ID */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人ID */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 逻辑删除:0-未删除,1-已删除 */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
