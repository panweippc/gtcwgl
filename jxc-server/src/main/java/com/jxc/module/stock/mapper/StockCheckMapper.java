package com.jxc.module.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxc.module.stock.entity.StockCheck;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存盘点主表 Mapper
 *
 * @author jxc
 */
@Mapper
public interface StockCheckMapper extends BaseMapper<StockCheck> {
}
