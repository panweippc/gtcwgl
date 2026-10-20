package com.jxc.module.customer.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jxc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 客户分类实体(树形结构)
 *
 * @author jxc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_category")
public class CustomerCategory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 父分类ID */
    private Long parentId;

    /** 分类名称 */
    private String categoryName;

    /** 分类编码 */
    private String categoryCode;

    /** 排序 */
    private Integer sort;

    /** 状态:0-禁用,1-启用 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 子分类(非数据库字段) */
    @TableField(exist = false)
    private List<CustomerCategory> children;
}
