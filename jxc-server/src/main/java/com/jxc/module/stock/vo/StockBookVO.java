package com.jxc.module.stock.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 库存账面视图对象(盘点时自动带出的账面库存)
 *
 * @author jxc
 */
@Data
public class StockBookVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 仓库ID */
    private Long warehouseId;

    /** 仓库名称 */
    private String warehouseName;

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

    /** 账面库存数量 */
    private Integer bookQuantity;
}
