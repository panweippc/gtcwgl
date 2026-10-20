package com.jxc.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxc.module.product.entity.Product;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 商品 Mapper
 *
 * @author jxc
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /** 查询采购明细中引用该商品的行数 */
    @Select("SELECT COUNT(*) FROM purchase_order_detail WHERE product_id = #{productId}")
    int countPurchase(@Param("productId") Long productId);

    /** 查询销售明细中引用该商品的行数 */
    @Select("SELECT COUNT(*) FROM sale_order_detail WHERE product_id = #{productId}")
    int countSale(@Param("productId") Long productId);

    /** 查询采购退货明细中引用该商品的行数 */
    @Select("SELECT COUNT(*) FROM purchase_return_order_detail WHERE product_id = #{productId}")
    int countPurchaseReturn(@Param("productId") Long productId);

    /** 查询销售退货明细中引用该商品的行数 */
    @Select("SELECT COUNT(*) FROM sale_return_order_detail WHERE product_id = #{productId}")
    int countSaleReturn(@Param("productId") Long productId);

    /** 查询库存表中引用该商品的行数 */
    @Select("SELECT COUNT(*) FROM stock WHERE product_id = #{productId} AND deleted = 0")
    int countStock(@Param("productId") Long productId);

    /** 物理删除(绕过逻辑删除时唯一索引冲突) */
    @Delete("DELETE FROM product WHERE id = #{productId} AND deleted = 0")
    int physicalDelete(@Param("productId") Long productId);
}
