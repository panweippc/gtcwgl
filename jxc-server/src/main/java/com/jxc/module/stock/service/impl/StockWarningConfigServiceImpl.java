package com.jxc.module.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.module.stock.entity.StockWarningConfig;
import com.jxc.module.stock.mapper.StockWarningConfigMapper;
import com.jxc.module.stock.service.StockWarningConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 库存预警配置服务实现
 *
 * @author jxc
 */
@Service
public class StockWarningConfigServiceImpl extends ServiceImpl<StockWarningConfigMapper, StockWarningConfig> implements StockWarningConfigService {

    @Override
    public List<StockWarningConfig> listAllEnabled() {
        LambdaQueryWrapper<StockWarningConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockWarningConfig::getStatus, 1).orderByAsc(StockWarningConfig::getId);
        return list(wrapper);
    }
}
