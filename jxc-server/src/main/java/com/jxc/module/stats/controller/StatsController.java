package com.jxc.module.stats.controller;

import com.jxc.common.Result;
import com.jxc.module.stats.service.StatsService;
import com.jxc.module.stats.vo.DashboardVO;
import com.jxc.module.stats.vo.PurchaseStatVO;
import com.jxc.module.stats.vo.PurchaseReturnStatVO;
import com.jxc.module.stats.vo.PurchaseSummaryVO;
import com.jxc.module.stats.vo.SaleReturnStatVO;
import com.jxc.module.stats.vo.SaleStatVO;
import com.jxc.module.stats.vo.SaleSummaryVO;
import com.jxc.module.stats.vo.StockStatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据统计控制器
 * 提供采购、销售、库存的汇总统计接口
 *
 * @author jxc
 */
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 首页看板
     * 默认统计全部已审核数据
     *
     * @return 看板汇总数据
     */
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(statsService.dashboard());
    }

    /**
     * 按日采购统计
     *
     * @param startDate 起始日期(yyyy-MM-dd)
     * @param endDate   结束日期(yyyy-MM-dd)
     * @return 按日聚合的采购统计列表
     */
    @GetMapping("/purchase/daily")
    public Result<List<PurchaseStatVO>> dailyPurchase(@RequestParam String startDate,
                                                      @RequestParam String endDate) {
        return Result.success(statsService.dailyPurchase(startDate, endDate));
    }

    /**
     * 采购汇总统计
     * 返回指定时间段内采购入库与采购退货的总数量、总金额
     *
     * @param startDate 起始日期(yyyy-MM-dd)
     * @param endDate   结束日期(yyyy-MM-dd)
     * @return 采购/退货汇总数据
     */
    @GetMapping("/purchase/summary")
    public Result<PurchaseSummaryVO> purchaseSummary(@RequestParam String startDate,
                                                     @RequestParam String endDate) {
        return Result.success(statsService.purchaseSummary(startDate, endDate));
    }

    /**
     * 按日采购退货统计
     *
     * @param startDate 起始日期(yyyy-MM-dd)
     * @param endDate   结束日期(yyyy-MM-dd)
     * @return 按日聚合的采购退货统计列表
     */
    @GetMapping("/purchase-return/daily")
    public Result<List<PurchaseReturnStatVO>> dailyPurchaseReturn(@RequestParam String startDate,
                                                                  @RequestParam String endDate) {
        return Result.success(statsService.dailyPurchaseReturn(startDate, endDate));
    }

    /**
     * 按日销售统计
     *
     * @param startDate 起始日期(yyyy-MM-dd)
     * @param endDate   结束日期(yyyy-MM-dd)
     * @return 按日聚合的销售统计列表
     */
    @GetMapping("/sale/daily")
    public Result<List<SaleStatVO>> dailySale(@RequestParam String startDate,
                                              @RequestParam String endDate) {
        return Result.success(statsService.dailySale(startDate, endDate));
    }

    /**
     * 销售汇总统计
     * 返回指定时间段内销售出库与销售退货的总数量、总金额
     *
     * @param startDate 起始日期(yyyy-MM-dd)
     * @param endDate   结束日期(yyyy-MM-dd)
     * @return 销售/退货汇总数据
     */
    @GetMapping("/sale/summary")
    public Result<SaleSummaryVO> saleSummary(@RequestParam String startDate,
                                             @RequestParam String endDate) {
        return Result.success(statsService.saleSummary(startDate, endDate));
    }

    /**
     * 按日销售退货统计
     *
     * @param startDate 起始日期(yyyy-MM-dd)
     * @param endDate   结束日期(yyyy-MM-dd)
     * @return 按日聚合的销售退货统计列表
     */
    @GetMapping("/sale-return/daily")
    public Result<List<SaleReturnStatVO>> dailySaleReturn(@RequestParam String startDate,
                                                          @RequestParam String endDate) {
        return Result.success(statsService.dailySaleReturn(startDate, endDate));
    }

    /**
     * 按仓库库存统计
     *
     * @return 按仓库聚合的库存统计列表
     */
    @GetMapping("/stock/warehouse")
    public Result<List<StockStatVO>> stockByWarehouse() {
        return Result.success(statsService.stockByWarehouse());
    }
}
