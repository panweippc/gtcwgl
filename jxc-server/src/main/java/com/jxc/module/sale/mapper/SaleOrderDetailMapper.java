package com.jxc.module.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxc.module.sale.entity.SaleOrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 销售出库明细 Mapper
 *
 * @author jxc
 */
@Mapper
public interface SaleOrderDetailMapper extends BaseMapper<SaleOrderDetail> {

    /**
     * 根据主表ID查询明细
     *
     * @param orderId 主表ID
     * @return 明细列表
     */
    @Select("SELECT * FROM sale_order_detail WHERE order_id = #{orderId} ORDER BY id ASC")
    List<SaleOrderDetail> selectByOrderId(@Param("orderId") Long orderId);
}
