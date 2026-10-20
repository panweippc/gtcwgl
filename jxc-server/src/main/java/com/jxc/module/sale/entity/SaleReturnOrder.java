package com.jxc.module.sale.entity;

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
 * 销售退货主表实体
 *
 * @author jxc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sale_return_order")
public class SaleReturnOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 退货单号 */
    private String orderNo;

    /** 客户ID */
    private Long customerId;

    /** 入库仓库ID */
    private Long warehouseId;

    /** 退货日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderTime;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 总数量 */
    private Integer totalQuantity;

    /** 应退金额 */
    private BigDecimal refundAmount;

    /** 退货原因 */
    private String returnReason;

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
    private List<SaleReturnOrderDetail> details;
}
