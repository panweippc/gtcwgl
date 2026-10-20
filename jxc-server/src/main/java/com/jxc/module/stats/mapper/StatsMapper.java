package com.jxc.module.stats.mapper;

import com.jxc.module.stats.vo.PurchaseStatVO;
import com.jxc.module.stats.vo.PurchaseReturnStatVO;
import com.jxc.module.stats.vo.PurchaseSummaryVO;
import com.jxc.module.stats.vo.SaleReturnStatVO;
import com.jxc.module.stats.vo.SaleStatVO;
import com.jxc.module.stats.vo.SaleSummaryVO;
import com.jxc.module.stats.vo.StockStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 数据统计 Mapper
 * 提供采购、销售、库存的聚合统计查询
 * 所有业务单据统计仅包含已审核(deleted=0 且 audit_status=1)数据
 *
 * @author jxc
 */
@Mapper
public interface StatsMapper {

    /**
     * 统计指定时间段的采购总金额(已审核)
     *
     * @param startTime 起始时间(含)
     * @param endTime   结束时间(含)
     * @return 采购总金额, 无数据返回0
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM purchase_order " +
            "WHERE audit_status = 1 AND deleted = 0 " +
            "AND order_time BETWEEN #{startTime} AND #{endTime}")
    BigDecimal countPurchaseAmount(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 统计指定时间段的销售总金额(已审核)
     *
     * @param startTime 起始时间(含)
     * @param endTime   结束时间(含)
     * @return 销售总金额, 无数据返回0
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM sale_order " +
            "WHERE audit_status = 1 AND deleted = 0 " +
            "AND order_time BETWEEN #{startTime} AND #{endTime}")
    BigDecimal countSaleAmount(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 统计指定时间段的采购单数(已审核)
     *
     * @param startTime 起始时间(含)
     * @param endTime   结束时间(含)
     * @return 采购单数
     */
    @Select("SELECT COUNT(*) FROM purchase_order " +
            "WHERE audit_status = 1 AND deleted = 0 " +
            "AND order_time BETWEEN #{startTime} AND #{endTime}")
    Integer countPurchaseOrders(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 统计指定时间段的销售单数(已审核)
     *
     * @param startTime 起始时间(含)
     * @param endTime   结束时间(含)
     * @return 销售单数
     */
    @Select("SELECT COUNT(*) FROM sale_order " +
            "WHERE audit_status = 1 AND deleted = 0 " +
            "AND order_time BETWEEN #{startTime} AND #{endTime}")
    Integer countSaleOrders(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 统计商品总数(未删除)
     *
     * @return 商品总数
     */
    @Select("SELECT COUNT(*) FROM product WHERE deleted = 0")
    Integer countProducts();

    /**
     * 统计商品种类数(商品分类数,未删除)
     *
     * @return 商品分类数
     */
    @Select("SELECT COUNT(*) FROM product_category WHERE deleted = 0")
    Integer countProductCategories();

    /**
     * 统计库存总数量(未删除)
     *
     * @return 库存总数量, 无数据返回0
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM stock WHERE deleted = 0")
    Integer countStockQuantity();

    /**
     * 统计库存预警数
     * 与库存预警页面规则保持一致:
     * 按每条 stock 记录(仓库+商品维度)判断
     * - 库存不足: quantity < min_stock
     * - 库存积压: quantity > max_stock
     *
     * @return 预警记录数(库存不足+库存积压)
     */
    @Select("SELECT COUNT(*) FROM stock s " +
            "LEFT JOIN product p ON p.id = s.product_id AND p.deleted = 0 " +
            "WHERE s.deleted = 0 " +
            "  AND p.deleted = 0 " +
            "  AND ( " +
            "    (p.min_stock IS NOT NULL AND p.min_stock > 0 AND s.quantity < p.min_stock) " +
            "    OR " +
            "    (p.max_stock IS NOT NULL AND p.max_stock > 0 AND s.quantity > p.max_stock) " +
            "  )")
    Integer countStockWarning();

    /**
     * 汇总统计指定时间段的采购与采购退货总数量、总金额(均为已审核单据)
     * 采购取 purchase_order, 退货取 purchase_return_order
     *
     * @param startTime 起始时间(含)
     * @param endTime   结束时间(含)
     * @return 采购/退货汇总数据, 无数据时各指标为0
     */
    @Select("SELECT " +
            "COALESCE((SELECT SUM(total_quantity) FROM purchase_order " +
            "          WHERE audit_status = 1 AND deleted = 0 " +
            "          AND order_time BETWEEN #{startTime} AND #{endTime}), 0) AS purchase_total_quantity, " +
            "COALESCE((SELECT SUM(total_amount) FROM purchase_order " +
            "          WHERE audit_status = 1 AND deleted = 0 " +
            "          AND order_time BETWEEN #{startTime} AND #{endTime}), 0) AS purchase_total_amount, " +
            "COALESCE((SELECT SUM(total_quantity) FROM purchase_return_order " +
            "          WHERE audit_status = 1 AND deleted = 0 " +
            "          AND order_time BETWEEN #{startTime} AND #{endTime}), 0) AS return_total_quantity, " +
            "COALESCE((SELECT SUM(total_amount) FROM purchase_return_order " +
            "          WHERE audit_status = 1 AND deleted = 0 " +
            "          AND order_time BETWEEN #{startTime} AND #{endTime}), 0) AS return_total_amount")
    PurchaseSummaryVO purchaseSummary(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 按日统计采购数据(已审核)
     *
     * @param startTime 起始时间(含)
     * @param endTime   结束时间(含)
     * @return 按日聚合的采购统计列表
     */
    @Select("SELECT DATE_FORMAT(order_time, '%Y-%m-%d') AS stat_date, " +
            "       SUM(total_amount) AS total_amount, " +
            "       SUM(total_quantity) AS total_quantity, " +
            "       COUNT(*) AS order_count " +
            "FROM purchase_order " +
            "WHERE audit_status = 1 AND deleted = 0 " +
            "  AND order_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY stat_date " +
            "ORDER BY stat_date")
    List<PurchaseStatVO> dailyPurchaseStat(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 按日统计采购退货数据(已审核)
     *
     * @param startTime 起始时间(含)
     * @param endTime   结束时间(含)
     * @return 按日聚合的采购退货统计列表
     */
    @Select("SELECT DATE_FORMAT(order_time, '%Y-%m-%d') AS stat_date, " +
            "       SUM(total_amount) AS total_amount, " +
            "       SUM(total_quantity) AS total_quantity, " +
            "       COUNT(*) AS order_count " +
            "FROM purchase_return_order " +
            "WHERE audit_status = 1 AND deleted = 0 " +
            "  AND order_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY stat_date " +
            "ORDER BY stat_date")
    List<PurchaseReturnStatVO> dailyPurchaseReturnStat(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 按日统计销售数据(已审核)
     *
     * @param startTime 起始时间(含)
     * @param endTime   结束时间(含)
     * @return 按日聚合的销售统计列表
     */
    @Select("SELECT DATE_FORMAT(order_time, '%Y-%m-%d') AS stat_date, " +
            "       SUM(total_amount) AS total_amount, " +
            "       SUM(total_quantity) AS total_quantity, " +
            "       COUNT(*) AS order_count " +
            "FROM sale_order " +
            "WHERE audit_status = 1 AND deleted = 0 " +
            "  AND order_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY stat_date " +
            "ORDER BY stat_date")
    List<SaleStatVO> dailySaleStat(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 汇总统计指定时间段的销售与销售退货总数量、总金额(均为已审核单据)
     * 销售取 sale_order, 退货取 sale_return_order
     *
     * @param startTime 起始时间(含)
     * @param endTime   结束时间(含)
     * @return 销售/退货汇总数据, 无数据时各指标为0
     */
    @Select("SELECT " +
            "COALESCE((SELECT SUM(total_quantity) FROM sale_order " +
            "          WHERE audit_status = 1 AND deleted = 0 " +
            "          AND order_time BETWEEN #{startTime} AND #{endTime}), 0) AS sale_total_quantity, " +
            "COALESCE((SELECT SUM(total_amount) FROM sale_order " +
            "          WHERE audit_status = 1 AND deleted = 0 " +
            "          AND order_time BETWEEN #{startTime} AND #{endTime}), 0) AS sale_total_amount, " +
            "COALESCE((SELECT SUM(total_quantity) FROM sale_return_order " +
            "          WHERE audit_status = 1 AND deleted = 0 " +
            "          AND order_time BETWEEN #{startTime} AND #{endTime}), 0) AS return_total_quantity, " +
            "COALESCE((SELECT SUM(total_amount) FROM sale_return_order " +
            "          WHERE audit_status = 1 AND deleted = 0 " +
            "          AND order_time BETWEEN #{startTime} AND #{endTime}), 0) AS return_total_amount")
    SaleSummaryVO saleSummary(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 按日统计销售退货数据(已审核)
     *
     * @param startTime 起始时间(含)
     * @param endTime   结束时间(含)
     * @return 按日聚合的销售退货统计列表
     */
    @Select("SELECT DATE_FORMAT(order_time, '%Y-%m-%d') AS stat_date, " +
            "       SUM(total_amount) AS total_amount, " +
            "       SUM(total_quantity) AS total_quantity, " +
            "       COUNT(*) AS order_count " +
            "FROM sale_return_order " +
            "WHERE audit_status = 1 AND deleted = 0 " +
            "  AND order_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY stat_date " +
            "ORDER BY stat_date")
    List<SaleReturnStatVO> dailySaleReturnStat(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 按仓库统计库存
     * 关联仓库表获取仓库名称, 关联商品表计算库存成本
     *
     * @return 按仓库聚合的库存统计列表
     */
    @Select("SELECT s.warehouse_id AS warehouse_id, " +
            "       w.warehouse_name AS warehouse_name, " +
            "       COUNT(DISTINCT s.product_id) AS product_count, " +
            "       SUM(s.quantity) AS total_quantity, " +
            "       SUM(s.quantity * p.cost_price) AS total_cost " +
            "FROM stock s " +
            "LEFT JOIN warehouse w ON w.id = s.warehouse_id " +
            "LEFT JOIN product p ON p.id = s.product_id " +
            "WHERE s.deleted = 0 AND w.deleted = 0 " +
            "GROUP BY s.warehouse_id, w.warehouse_name")
    List<StockStatVO> stockStatByWarehouse();
}
