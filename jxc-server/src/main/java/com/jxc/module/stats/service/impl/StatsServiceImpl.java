package com.jxc.module.stats.service.impl;

import com.jxc.module.stats.mapper.StatsMapper;
import com.jxc.module.stats.service.StatsService;
import com.jxc.module.stats.vo.DashboardVO;
import com.jxc.module.stats.vo.PurchaseStatVO;
import com.jxc.module.stats.vo.PurchaseReturnStatVO;
import com.jxc.module.stats.vo.PurchaseSummaryVO;
import com.jxc.module.stats.vo.SaleReturnStatVO;
import com.jxc.module.stats.vo.SaleStatVO;
import com.jxc.module.stats.vo.SaleSummaryVO;
import com.jxc.module.stats.vo.StockStatVO;
import com.jxc.module.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 数据统计服务实现
 * 组合调用 StatsMapper 提供采购、销售、库存的汇总统计
 *
 * @author jxc
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    /** 日期格式(yyyy-MM-dd) */
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private final StatsMapper statsMapper;

    /** 注入 StockService 复用已验证的库存预警查询 */
    private final StockService stockService;

    @Override
    public DashboardVO dashboard() {
        DashboardVO vo = new DashboardVO();
        // 看板默认统计全部已审核数据,时间范围不限
        Date startTime = new Date(0);
        Date endTime = new Date();
        vo.setPurchaseTotalAmount(statsMapper.countPurchaseAmount(startTime, endTime));
        vo.setSaleTotalAmount(statsMapper.countSaleAmount(startTime, endTime));
        vo.setPurchaseOrderCount(statsMapper.countPurchaseOrders(startTime, endTime));
        vo.setSaleOrderCount(statsMapper.countSaleOrders(startTime, endTime));
        vo.setProductCount(statsMapper.countProducts());
        // 商品种类数: 按库存中 product_id 去重统计(同一商品在多仓库只计1种)
        // 与库存查询页面 loadProductCategoryCount 逻辑保持一致
        vo.setProductCategoryCount(stockService.productCategoryCount(null).intValue());
        vo.setStockTotalQuantity(statsMapper.countStockQuantity());
        // 库存预警数: 直接复用 StockService.warningList(null).size()
        // 确保与库存预警页面查询结果完全一致
        vo.setStockWarningCount(stockService.warningList(null).size());
        return vo;
    }

    @Override
    public List<PurchaseStatVO> dailyPurchase(String startDate, String endDate) {
        Date[] range = parseDateRange(startDate, endDate);
        return statsMapper.dailyPurchaseStat(range[0], range[1]);
    }

    @Override
    public PurchaseSummaryVO purchaseSummary(String startDate, String endDate) {
        Date[] range = parseDateRange(startDate, endDate);
        return statsMapper.purchaseSummary(range[0], range[1]);
    }

    @Override
    public List<PurchaseReturnStatVO> dailyPurchaseReturn(String startDate, String endDate) {
        Date[] range = parseDateRange(startDate, endDate);
        return statsMapper.dailyPurchaseReturnStat(range[0], range[1]);
    }

    @Override
    public List<SaleStatVO> dailySale(String startDate, String endDate) {
        Date[] range = parseDateRange(startDate, endDate);
        return statsMapper.dailySaleStat(range[0], range[1]);
    }

    @Override
    public SaleSummaryVO saleSummary(String startDate, String endDate) {
        Date[] range = parseDateRange(startDate, endDate);
        return statsMapper.saleSummary(range[0], range[1]);
    }

    @Override
    public List<SaleReturnStatVO> dailySaleReturn(String startDate, String endDate) {
        Date[] range = parseDateRange(startDate, endDate);
        return statsMapper.dailySaleReturnStat(range[0], range[1]);
    }

    @Override
    public List<StockStatVO> stockByWarehouse() {
        return statsMapper.stockStatByWarehouse();
    }

    /**
     * 解析日期范围字符串为Date数组
     * 起始日期取当天0点, 结束日期取当天23:59:59
     *
     * @param startDate 起始日期字符串(yyyy-MM-dd)
     * @param endDate   结束日期字符串(yyyy-MM-dd)
     * @return Date数组, 下标0为起始, 下标1为结束
     */
    private Date[] parseDateRange(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            // 结束日期延后到当天23:59:59, 保证 BETWEEN 闭区间覆盖整天
            Calendar cal = Calendar.getInstance();
            cal.setTime(end);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            return new Date[]{start, cal.getTime()};
        } catch (ParseException e) {
            log.error("日期解析失败, startDate={}, endDate={}", startDate, endDate, e);
            throw new IllegalArgumentException("日期格式错误, 正确格式为 yyyy-MM-dd");
        }
    }
}
