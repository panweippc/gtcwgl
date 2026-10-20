package com.jxc.module.stock.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jxc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存实体
 *
 * @author jxc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock")
public class Stock extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 仓库ID */
    private Long warehouseId;

    /** 商品ID */
    private Long productId;

    /** 库存数量 */
    private Integer quantity;

    /** 锁定数量 */
    private Integer lockQuantity;
}
