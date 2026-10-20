package com.jxc.module.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jxc.module.stock.entity.Stock;
import com.jxc.module.stock.vo.StockBookVO;
import com.jxc.module.stock.vo.StockFlowVO;
import com.jxc.module.stock.vo.StockWarningVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 库存 Mapper
 *
 * @author jxc
 */
@Mapper
public interface StockMapper extends BaseMapper<Stock> {

    /**
     * 原子增加库存(用于采购入库/调拨入库)
     * 库存记录不存在时返回0,由 service 调用方先创建
     *
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @param quantity    增加数量(正数)
     * @return 受影响行数
     */
    @Update("UPDATE stock SET quantity = quantity + #{quantity}, update_time = NOW() " +
            "WHERE warehouse_id = #{warehouseId} AND product_id = #{productId} AND deleted = 0")
    int addQuantity(@Param("warehouseId") Long warehouseId,
                    @Param("productId") Long productId,
                    @Param("quantity") Integer quantity);

    /**
     * 原子扣减库存(用于销售出库/调拨出库)
     * 通过 quantity >= #{quantity} 条件防止负库存
     *
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @param quantity    扣减数量(正数)
     * @return 受影响行数:1-成功,0-库存不足或记录不存在
     */
    @Update("UPDATE stock SET quantity = quantity - #{quantity}, update_time = NOW() " +
            "WHERE warehouse_id = #{warehouseId} AND product_id = #{productId} " +
            "AND quantity >= #{quantity} AND deleted = 0")
    int deductQuantity(@Param("warehouseId") Long warehouseId,
                       @Param("productId") Long productId,
                       @Param("quantity") Integer quantity);

    /**
     * 根据仓库和商品查询库存记录
     *
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @return 库存
     */
    @Select("SELECT * FROM stock WHERE warehouse_id = #{warehouseId} AND product_id = #{productId} AND deleted = 0")
    Stock selectByWarehouseAndProduct(@Param("warehouseId") Long warehouseId,
                                      @Param("productId") Long productId);

    /**
     * 分页查询库存(联表仓库和商品)
     *
     * @param page        分页
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @param productName 商品名称
     * @param productCode 商品编码
     * @return 分页
     */
    IPage<StockWarningVO> pageStock(Page<StockWarningVO> page,
                                    @Param("warehouseId") Long warehouseId,
                                    @Param("productId") Long productId,
                                    @Param("productName") String productName,
                                    @Param("productCode") String productCode);

    /**
     * 查询库存预警列表
     * 条件:库存数量低于商品最低库存 或 高于商品最高库存
     *
     * @param warehouseId 仓库ID(可选)
     * @return 预警列表
     */
    List<StockWarningVO> selectWarningList(@Param("warehouseId") Long warehouseId);

    /**
     * 分页查询库存预警
     *
     * @param page        分页参数
     * @param warehouseId 仓库ID(可选)
     * @return 分页预警
     */
    IPage<StockWarningVO> pageWarning(Page<StockWarningVO> page,
                                     @Param("warehouseId") Long warehouseId,
                                     @Param("warningType") Integer warningType);

    /**
     * 分页查询库存变动流水(UNION 采购入库、采购退货、销售出库、销售退货、调拨、盘盈、盘亏)
     *
     * @param page      分页参数
     * @param productId 商品ID(必填)
     * @return 流水分页
     */
    IPage<StockFlowVO> pageStockFlow(Page<StockFlowVO> page,
                                     @Param("productId") Long productId);

    /**
     * 查询指定仓库的账面库存列表(盘点用,含数量为0的记录)
     *
     * @param warehouseId 仓库ID
     * @return 账面库存列表
     */
    List<StockBookVO> selectBookList(@Param("warehouseId") Long warehouseId);

    /**
     * 统计指定仓库(或全部仓库)的去重商品种类数
     *
     * @param warehouseId 仓库ID(null时统计全部仓库)
     * @return 商品种类数
     */
    Long countProductCategory(@Param("warehouseId") Long warehouseId);
}
