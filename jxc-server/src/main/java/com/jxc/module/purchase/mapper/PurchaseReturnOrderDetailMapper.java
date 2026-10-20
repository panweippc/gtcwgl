package com.jxc.module.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxc.module.purchase.entity.PurchaseReturnOrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 采购退货明细 Mapper
 *
 * @author jxc
 */
@Mapper
public interface PurchaseReturnOrderDetailMapper extends BaseMapper<PurchaseReturnOrderDetail> {

    /**
     * 根据主表ID查询明细
     *
     * @param orderId 主表ID
     * @return 明细列表
     */
    @Select("SELECT * FROM purchase_return_order_detail WHERE order_id = #{orderId} ORDER BY id ASC")
    List<PurchaseReturnOrderDetail> selectByOrderId(@Param("orderId") Long orderId);
}
