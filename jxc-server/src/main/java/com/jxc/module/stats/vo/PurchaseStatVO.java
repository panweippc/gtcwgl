package com.jxc.module.stats.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 采购统计VO
 * 按日聚合的采购汇总数据
 *
 * @author jxc
 */
@Data
public class PurchaseStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 统计日期(yyyy-MM-dd) */
    private String statDate;

    /** 当日采购总金额 */
    private BigDecimal totalAmount;

    /** 当日采购总数量 */
    private Integer totalQuantity;

    /** 当日采购单数 */
    private Integer orderCount;
}
