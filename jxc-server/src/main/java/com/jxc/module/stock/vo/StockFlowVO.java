package com.jxc.module.stock.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 库存变动流水视图对象
 * 由采购入库、采购退货、销售出库、销售退货、调拨单的已审核明细 UNION 聚合而成
 *
 * @author jxc
 */
@Data
public class StockFlowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务类型:PO-采购入库,PR-采购退货,SO-销售出库,SR-销售退货,TR-调拨入库,TO-调拨出库 */
    private String orderType;

    /** 业务类型中文 */
    private String orderTypeLabel;

    /** 单据编号 */
    private String orderNo;

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

    /** 变动数量(正数表示入库/增加,负数表示出库/扣减) */
    private Integer quantity;

    /** 变动方向标识:IN-入库,OUT-出库 */
    private String direction;

    /** 关联仓库(调拨专用,调拨入库时为调出仓库,调拨出库时为调入仓库) */
    private String relatedWarehouseName;

    /** 备注 */
    private String remark;

    /** 审核时间(即实际影响库存的时间) */
    private Date auditTime;

    /** 单据创建时间 */
    private Date createTime;
}
