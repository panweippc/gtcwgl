package com.jxc.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 采购退货明细实体
 *
 * @author jxc
 */
@Data
@TableName("purchase_return_order_detail")
public class PurchaseReturnOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 主表ID */
    private Long orderId;

    /** 退货单号 */
    private String orderNo;

    /** 商品ID */
    private Long productId;

    /** 商品编码 */
    private String productCode;

    /** 商品名称 */
    private String productName;

    /** 规格 */
    private String spec;

    /** 单位(录入单位:基本单位或大单位) */
    private String unit;

    /** 录入数量(按录入单位) */
    private Integer inputQuantity;

    /** 库存数量(换算为基本单位后的数量) */
    private Integer quantity;

    /** 退货单价 */
    private BigDecimal price;

    /** 金额 */
    private BigDecimal amount;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
