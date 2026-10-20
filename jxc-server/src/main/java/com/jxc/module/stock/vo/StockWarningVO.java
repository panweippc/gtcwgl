package com.jxc.module.stock.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 库存预警视图对象
 *
 * @author jxc
 */
@Data
public class StockWarningVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 库存ID */
    private Long id;

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

    /** 当前库存 */
    private Integer quantity;

    /** 最低库存 */
    private Integer minStock;

    /** 最高库存 */
    private Integer maxStock;

    /** 预警类型:1-库存不足,2-库存积压 */
    private Integer warningType;

    /** 成本价 */
    private BigDecimal costPrice;

    /** 该商品在所有仓库的总库存数量(聚合字段) */
    private Integer totalStock;
}
