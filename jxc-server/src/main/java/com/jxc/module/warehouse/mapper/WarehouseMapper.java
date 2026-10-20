package com.jxc.module.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxc.module.warehouse.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 仓库 Mapper
 *
 * @author jxc
 */
@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {

    /**
     * 统计该仓库下存在库存记录的商品数(库存数量大于0)
     *
     * @param warehouseId 仓库ID
     * @return 库存记录数
     */
    @Select("SELECT COUNT(*) FROM stock WHERE warehouse_id = #{warehouseId} AND deleted = 0 AND quantity > 0")
    int countStock(@Param("warehouseId") Long warehouseId);
}
