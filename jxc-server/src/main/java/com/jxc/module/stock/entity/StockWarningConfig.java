package com.jxc.module.stock.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jxc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存预警配置实体
 *
 * @author jxc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_warning_config")
public class StockWarningConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 规则名称 */
    private String name;

    /** 预警类型:1-最低库存,2-最高库存 */
    private Integer warningType;

    /** 默认最低库存 */
    private Integer defaultMin;

    /** 默认最高库存 */
    private Integer defaultMax;

    /** 状态:0-禁用,1-启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
