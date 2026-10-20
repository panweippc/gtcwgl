package com.jxc.module.stock.controller;

import com.jxc.common.Result;
import com.jxc.module.stock.entity.StockWarningConfig;
import com.jxc.module.stock.service.StockWarningConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 库存预警配置控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/stock/warning-config")
@RequiredArgsConstructor
public class StockWarningConfigController {

    private final StockWarningConfigService configService;

    /**
     * 查询全部启用配置
     */
    @GetMapping("/all")
    public Result<List<StockWarningConfig>> all() {
        return Result.success(configService.listAllEnabled());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public Result<StockWarningConfig> get(@PathVariable Long id) {
        return Result.success(configService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Void> add(@RequestBody StockWarningConfig config) {
        configService.save(config);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping
    public Result<Void> update(@RequestBody StockWarningConfig config) {
        configService.updateById(config);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        configService.removeById(id);
        return Result.success();
    }
}
