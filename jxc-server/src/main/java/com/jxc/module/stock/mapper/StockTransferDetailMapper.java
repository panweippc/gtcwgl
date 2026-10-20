package com.jxc.module.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxc.module.stock.entity.StockTransferDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 库存调拨明细 Mapper
 *
 * @author jxc
 */
@Mapper
public interface StockTransferDetailMapper extends BaseMapper<StockTransferDetail> {

    /**
     * 根据主表ID查询明细
     *
     * @param orderId 主表ID
     * @return 明细列表
     */
    @Select("SELECT * FROM stock_transfer_detail WHERE order_id = #{orderId} ORDER BY id ASC")
    List<StockTransferDetail> selectByOrderId(@Param("orderId") Long orderId);
}
