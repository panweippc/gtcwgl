package com.jxc.module.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxc.module.sale.entity.SaleOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售出库主表 Mapper
 *
 * @author jxc
 */
@Mapper
public interface SaleOrderMapper extends BaseMapper<SaleOrder> {
}
