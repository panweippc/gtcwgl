package com.jxc.module.stock.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jxc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 库存调拨主表实体
 *
 * @author jxc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_transfer")
public class StockTransfer extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 单据编号 */
    private String orderNo;

    /** 调出仓库ID */
    private Long fromWarehouseId;

    /** 调入仓库ID */
    private Long toWarehouseId;

    /** 调拨日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderTime;

    /** 总数量 */
    private Integer totalQuantity;

    /** 审核状态:0-待审核,1-已审核 */
    private Integer auditStatus;

    /** 审核人ID */
    private Long auditUserId;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditTime;

    /** 备注 */
    private String remark;

    /** 调拨明细(非数据库字段) */
    @TableField(exist = false)
    private List<StockTransferDetail> details;
}
