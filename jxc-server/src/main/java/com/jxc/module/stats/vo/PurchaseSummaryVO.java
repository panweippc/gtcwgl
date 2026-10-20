package com.jxc.module.stats.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 采购汇总统计VO
 * 统计指定时间段内采购入库与采购退货的总数量、总金额
 *
 * @author jxc
 */
@Data
public class PurchaseSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 采购总数量 */
    private Integer purchaseTotalQuantity;

    /** 采购总金额 */
    private BigDecimal purchaseTotalAmount;

    /** 采购退货总数量 */
    private Integer returnTotalQuantity;

    /** 采购退货总金额 */
    private BigDecimal returnTotalAmount;
}
