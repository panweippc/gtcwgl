package com.jxc.module.stats.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售统计VO
 * 按日聚合的销售汇总数据
 *
 * @author jxc
 */
@Data
public class SaleStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 统计日期(yyyy-MM-dd) */
    private String statDate;

    /** 当日销售总金额 */
    private BigDecimal totalAmount;

    /** 当日销售总数量 */
    private Integer totalQuantity;

    /** 当日销售单数 */
    private Integer orderCount;
}
