package com.jxc.module.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxc.module.stock.entity.StockCheckDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 库存盘点明细 Mapper
 *
 * @author jxc
 */
@Mapper
public interface StockCheckDetailMapper extends BaseMapper<StockCheckDetail> {

    /**
     * 根据主表ID查询明细
     *
     * @param orderId 主表ID
     * @return 明细列表
     */
    @Select("SELECT * FROM stock_check_detail WHERE order_id = #{orderId} ORDER BY id ASC")
    List<StockCheckDetail> selectByOrderId(@Param("orderId") Long orderId);
}
