package com.jxc.module.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jxc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品实体
 *
 * @author jxc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 商品编码 */
    private String productCode;

    /** 商品名称 */
    private String productName;

    /** 商品分类ID */
    private Long categoryId;

    /** 规格 */
    private String spec;

    /** 单位(基本单位/最小单位) */
    private String unit;

    /** 大单位,空则无大单位 */
    private String bigUnit;

    /** 换算率:1大单位=N基本单位 */
    private Integer conversionRate;

    /** 条形码 */
    private String barcode;

    /** 成本价 */
    private BigDecimal costPrice;

    /** 销售价 */
    private BigDecimal salePrice;

    /** 最低库存预警 */
    private Integer minStock;

    /** 最高库存预警 */
    private Integer maxStock;

    /** 状态:0-停售,1-启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
