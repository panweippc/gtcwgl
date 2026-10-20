package com.jxc.module.supplier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxc.module.supplier.entity.Supplier;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 供应商 Mapper
 *
 * @author jxc
 */
@Mapper
public interface SupplierMapper extends BaseMapper<Supplier> {

    /**
     * 统计采购入库单中引用该供应商的单据数
     *
     * @param supplierId 供应商ID
     * @return 采购单据数
     */
    @Select("SELECT COUNT(*) FROM purchase_order WHERE supplier_id = #{supplierId} AND deleted = 0")
    int countPurchase(@Param("supplierId") Long supplierId);

    /**
     * 统计采购退货单中引用该供应商的单据数
     *
     * @param supplierId 供应商ID
     * @return 采购退货单据数
     */
    @Select("SELECT COUNT(*) FROM purchase_return_order WHERE supplier_id = #{supplierId} AND deleted = 0")
    int countPurchaseReturn(@Param("supplierId") Long supplierId);
}
