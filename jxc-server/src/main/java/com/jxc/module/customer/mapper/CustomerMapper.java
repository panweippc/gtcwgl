package com.jxc.module.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxc.module.customer.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 客户 Mapper
 *
 * @author jxc
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

    /**
     * 统计销售出库单中引用该客户的单据数
     *
     * @param customerId 客户ID
     * @return 销售单据数
     */
    @Select("SELECT COUNT(*) FROM sale_order WHERE customer_id = #{customerId} AND deleted = 0")
    int countSale(@Param("customerId") Long customerId);

    /**
     * 统计销售退货单中引用该客户的单据数
     *
     * @param customerId 客户ID
     * @return 销售退货单据数
     */
    @Select("SELECT COUNT(*) FROM sale_return_order WHERE customer_id = #{customerId} AND deleted = 0")
    int countSaleReturn(@Param("customerId") Long customerId);
}
