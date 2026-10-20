package com.jxc.module.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxc.module.stock.entity.StockTransfer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存调拨主表 Mapper
 *
 * @author jxc
 */
@Mapper
public interface StockTransferMapper extends BaseMapper<StockTransfer> {
}
