package com.jxc.module.stats.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 库存统计VO
 * 按仓库聚合的库存汇总数据
 *
 * @author jxc
 */
@Data
public class StockStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 仓库ID */
    private Long warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    /** 商品数量(不同商品数) */
    private Integer productCount;

    /** 总库存数量 */
    private Integer totalQuantity;

    /** 库存成本(按成本价计算) */
    private BigDecimal totalCost;
}
