package com.jxc.module.stock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.module.stock.entity.StockWarningConfig;

import java.util.List;

/**
 * 库存预警配置服务接口
 *
 * @author jxc
 */
public interface StockWarningConfigService extends IService<StockWarningConfig> {

    /**
     * 查询全部启用配置
     *
     * @return 配置列表
     */
    List<StockWarningConfig> listAllEnabled();
}
