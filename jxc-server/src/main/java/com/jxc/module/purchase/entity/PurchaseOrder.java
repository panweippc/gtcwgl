package com.jxc.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jxc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 采购入库主表实体
 *
 * @author jxc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_order")
public class PurchaseOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 单据编号 */
    private String orderNo;

    /** 供应商ID */
    private Long supplierId;

    /** 入库仓库ID */
    private Long warehouseId;

    /** 采购日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderTime;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 总数量 */
    private Integer totalQuantity;

    /** 已付金额 */
    private BigDecimal payAmount;

    /** 审核状态:0-待审核,1-已审核 */
    private Integer auditStatus;

    /** 审核人ID */
    private Long auditUserId;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditTime;

    /** 业务员ID */
    private Long operatorId;

    /** 备注 */
    private String remark;

    /** 明细列表(非数据库字段) */
    @TableField(exist = false)
    private List<PurchaseOrderDetail> details;
}
