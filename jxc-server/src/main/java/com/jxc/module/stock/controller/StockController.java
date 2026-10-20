package com.jxc.module.stock.controller;

import com.jxc.common.PageResult;
import com.jxc.common.Result;
import com.jxc.module.stock.service.StockService;
import com.jxc.module.stock.vo.StockFlowVO;
import com.jxc.module.stock.vo.StockWarningVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 库存控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * 分页查询库存
     */
    @GetMapping("/page")
    public Result<PageResult<StockWarningVO>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productCode) {
        return Result.success(stockService.page(page, size, warehouseId, productId, productName, productCode));
    }

    /**
     * 库存预警查询
     */
    @GetMapping("/warning")
    public Result<List<StockWarningVO>> warning(@RequestParam(required = false) Long warehouseId) {
        return Result.success(stockService.warningList(warehouseId));
    }

    /**
     * 库存预警分页查询
     */
    @GetMapping("/warning/page")
    public Result<PageResult<StockWarningVO>> warningPage(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer warningType) {
        return Result.success(stockService.warningPage(page, size, warehouseId, warningType));
    }

    /**
     * 查询某仓库某商品的当前库存
     */
    @GetMapping("/quantity")
    public Result<Integer> quantity(@RequestParam Long warehouseId, @RequestParam Long productId) {
        return Result.success(stockService.getQuantity(warehouseId, productId));
    }

    /**
     * 分页查询某商品的库存变动流水
     */
    @GetMapping("/flow")
    public Result<PageResult<StockFlowVO>> flow(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam Long productId) {
        return Result.success(stockService.flowPage(page, size, productId));
    }

    /**
     * 全部仓库所有商品的库存数量总和
     *
     * @param warehouseId 仓库ID(可选,为null时统计全部仓库)
     */
    @GetMapping("/total")
    public Result<Integer> totalStock(@RequestParam(required = false) Long warehouseId) {
        return Result.success(stockService.totalStock(warehouseId));
    }

    /**
     * 统计去重商品种类数(按仓库过滤)
     *
     * @param warehouseId 仓库ID(可选,为null时统计全部仓库)
     */
    @GetMapping("/product-category-count")
    public Result<Long> productCategoryCount(@RequestParam(required = false) Long warehouseId) {
        return Result.success(stockService.productCategoryCount(warehouseId));
    }
}
