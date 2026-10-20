package com.jxc.module.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jxc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 仓库实体
 *
 * @author jxc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("warehouse")
public class Warehouse extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 仓库编码 */
    private String warehouseCode;

    /** 仓库名称 */
    private String warehouseName;

    /** 地址 */
    private String address;

    /** 负责人 */
    private String manager;

    /** 联系电话 */
    private String phone;

    /** 排序 */
    private Integer sort;

    /** 状态:0-禁用,1-启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
