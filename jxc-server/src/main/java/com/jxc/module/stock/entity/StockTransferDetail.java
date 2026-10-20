package com.jxc.module.stock.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 库存调拨明细实体
 *
 * @author jxc
 */
@Data
@TableName("stock_transfer_detail")
public class StockTransferDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 主表ID */
    private Long orderId;

    /** 单据编号 */
    private String orderNo;

    /** 商品ID */
    private Long productId;

    /** 商品编码 */
    private String productCode;

    /** 商品名称 */
    private String productName;

    /** 规格 */
    private String spec;

    /** 单位 */
    private String unit;

    /** 调拨数量 */
    private Integer quantity;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
