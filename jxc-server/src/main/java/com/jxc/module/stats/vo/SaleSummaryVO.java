package com.jxc.module.stats.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售汇总统计VO
 * 统计指定时间段内销售出库与销售退货的总数量、总金额
 *
 * @author jxc
 */
@Data
public class SaleSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 销售总数量 */
    private Integer saleTotalQuantity;

    /** 销售总金额 */
    private BigDecimal saleTotalAmount;

    /** 销售退货总数量 */
    private Integer returnTotalQuantity;

    /** 销售退货总金额 */
    private BigDecimal returnTotalAmount;
}
