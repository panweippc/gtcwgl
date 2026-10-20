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
 * 库存盘点主表实体
 *
 * @author jxc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_check")
public class StockCheck extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 盘点单编号 */
    private String orderNo;

    /** 仓库ID */
    private Long warehouseId;

    /** 盘点日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderTime;

    /** 账面总数量 */
    private Integer totalBookQuantity;

    /** 实盘总数量 */
    private Integer totalActualQuantity;

    /** 盘盈总数量 */
    private Integer totalProfitQuantity;

    /** 盘亏总数量 */
    private Integer totalLossQuantity;

    /** 盘盈单号(盘点时自动生成) */
    private String profitOrderNo;

    /** 盘亏单号(盘点时自动生成) */
    private String lossOrderNo;

    /** 盘点状态:0-待盘点,1-已盘点 */
    private Integer auditStatus;

    /** 盘点人ID */
    private Long auditUserId;

    /** 盘点时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditTime;

    /** 备注 */
    private String remark;

    /** 盘点明细(非数据库字段) */
    @TableField(exist = false)
    private List<StockCheckDetail> details;
}
