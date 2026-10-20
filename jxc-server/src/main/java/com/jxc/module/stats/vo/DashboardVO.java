package com.jxc.module.stats.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 首页看板统计数据VO
 * 汇总采购、销售、库存等核心指标
 *
 * @author jxc
 */
@Data
public class DashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 采购总额(已审核) */
    private BigDecimal purchaseTotalAmount;

    /** 销售总额(已审核) */
    private BigDecimal saleTotalAmount;

    /** 采购单数(已审核) */
    private Integer purchaseOrderCount;

    /** 销售单数(已审核) */
    private Integer saleOrderCount;

    /** 商品总数 */
    private Integer productCount;

    /** 商品种类数(商品分类数) */
    private Integer productCategoryCount;

    /** 库存预警数(低于最低库存的商品数) */
    private Integer stockWarningCount;

    /** 库存总数量 */
    private Integer stockTotalQuantity;
}
