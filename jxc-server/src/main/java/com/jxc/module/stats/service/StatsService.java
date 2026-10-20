package com.jxc.module.stats.service;

import com.jxc.module.stats.vo.DashboardVO;
import com.jxc.module.stats.vo.PurchaseStatVO;
import com.jxc.module.stats.vo.PurchaseReturnStatVO;
import com.jxc.module.stats.vo.PurchaseSummaryVO;
import com.jxc.module.stats.vo.SaleReturnStatVO;
import com.jxc.module.stats.vo.SaleStatVO;
import com.jxc.module.stats.vo.SaleSummaryVO;
import com.jxc.module.stats.vo.StockStatVO;

import java.util.List;

/**
 * 数据统计服务接口
 * 提供采购、销售、库存的汇总统计能力
 *
 * @author jxc
 */
public interface StatsService {

    /**
     * 首页看板统计
     * 汇总采购、销售、商品、库存等核心指标
     *
     * @return 看板数据
     */
    DashboardVO dashboard();

    /**
     * 按日采购统计
     *
     * @param startDate 起始日期(yyyy-MM-dd)
     * @param endDate   结束日期(yyyy-MM-dd)
     * @return 按日聚合的采购统计列表
     */
    List<PurchaseStatVO> dailyPurchase(String startDate, String endDate);

    /**
     * 采购汇总统计
     * 汇总指定时间段内采购入库与采购退货的总数量、总金额
     *
     * @param startDate 起始日期(yyyy-MM-dd)
     * @param endDate   结束日期(yyyy-MM-dd)
     * @return 采购/退货汇总数据
     */
    PurchaseSummaryVO purchaseSummary(String startDate, String endDate);

    /**
     * 按日采购退货统计
     *
     * @param startDate 起始日期(yyyy-MM-dd)
     * @param endDate   结束日期(yyyy-MM-dd)
     * @return 按日聚合的采购退货统计列表
     */
    List<PurchaseReturnStatVO> dailyPurchaseReturn(String startDate, String endDate);

    /**
     * 按日销售统计
     *
     * @param startDate 起始日期(yyyy-MM-dd)
     * @param endDate   结束日期(yyyy-MM-dd)
     * @return 按日聚合的销售统计列表
     */
    List<SaleStatVO> dailySale(String startDate, String endDate);

    /**
     * 销售汇总统计
     * 汇总指定时间段内销售出库与销售退货的总数量、总金额
     *
     * @param startDate 起始日期(yyyy-MM-dd)
     * @param endDate   结束日期(yyyy-MM-dd)
     * @return 销售/退货汇总数据
     */
    SaleSummaryVO saleSummary(String startDate, String endDate);

    /**
     * 按日销售退货统计
     *
     * @param startDate 起始日期(yyyy-MM-dd)
     * @param endDate   结束日期(yyyy-MM-dd)
     * @return 按日聚合的销售退货统计列表
     */
    List<SaleReturnStatVO> dailySaleReturn(String startDate, String endDate);

    /**
     * 按仓库库存统计
     *
     * @return 按仓库聚合的库存统计列表
     */
    List<StockStatVO> stockByWarehouse();
}
