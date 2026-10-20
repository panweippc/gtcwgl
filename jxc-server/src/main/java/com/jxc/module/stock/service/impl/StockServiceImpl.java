package com.jxc.module.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.common.BusinessException;
import com.jxc.common.PageResult;
import com.jxc.common.ResultCode;
import com.jxc.module.stock.entity.Stock;
import com.jxc.module.stock.mapper.StockMapper;
import com.jxc.module.stock.service.StockService;
import com.jxc.module.stock.vo.StockFlowVO;
import com.jxc.module.stock.vo.StockWarningVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 库存服务实现
 * 核心规则:出入库必须事务控制、禁止负库存
 *
 * @author jxc
 */
@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService {

    @Override
    public PageResult<StockWarningVO> page(Long page, Long size, Long warehouseId,
                                          Long productId, String productName, String productCode) {
        Page<StockWarningVO> p = new Page<>(page, size);
        IPage<StockWarningVO> result = baseMapper.pageStock(p, warehouseId, productId, productName, productCode);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStock(Long warehouseId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("增加库存数量必须大于0");
        }
        // 先尝试原子增加
        int rows = baseMapper.addQuantity(warehouseId, productId, quantity);
        if (rows == 0) {
            // 库存记录不存在,自动创建
            Stock stock = new Stock();
            stock.setWarehouseId(warehouseId);
            stock.setProductId(productId);
            stock.setQuantity(quantity);
            stock.setLockQuantity(0);
            try {
                save(stock);
            } catch (Exception e) {
                // 并发场景:可能其他线程已创建,再次原子增加
                rows = baseMapper.addQuantity(warehouseId, productId, quantity);
                if (rows == 0) {
                    throw new BusinessException("增加库存失败");
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStock(Long warehouseId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("扣减库存数量必须大于0");
        }
        // 原子扣减,通过 SQL 条件 quantity >= #{quantity} 防止负库存
        int rows = baseMapper.deductQuantity(warehouseId, productId, quantity);
        if (rows == 0) {
            // 库存不足或记录不存在
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferStock(Long fromWarehouseId, Long toWarehouseId, Long productId, Integer quantity) {
        if (fromWarehouseId.equals(toWarehouseId)) {
            throw new BusinessException("调出仓库与调入仓库不能相同");
        }
        // 先扣减调出仓库(若库存不足会抛异常并回滚)
        deductStock(fromWarehouseId, productId, quantity);
        // 再增加到调入仓库
        addStock(toWarehouseId, productId, quantity);
    }

    @Override
    public List<StockWarningVO> warningList(Long warehouseId) {
        return baseMapper.selectWarningList(warehouseId);
    }

    @Override
    public PageResult<StockWarningVO> warningPage(Long page, Long size, Long warehouseId, Integer warningType) {
        Page<StockWarningVO> p = new Page<>(page, size);
        IPage<StockWarningVO> result = baseMapper.pageWarning(p, warehouseId, warningType);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public Integer getQuantity(Long warehouseId, Long productId) {
        Stock stock = baseMapper.selectByWarehouseAndProduct(warehouseId, productId);
        return stock == null ? 0 : stock.getQuantity();
    }

    @Override
    public PageResult<StockFlowVO> flowPage(Long page, Long size, Long productId) {
        Page<StockFlowVO> p = new Page<>(page, size);
        IPage<StockFlowVO> result = baseMapper.pageStockFlow(p, productId);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public Integer totalStock(Long warehouseId) {
        // 聚合查询库存数量之和,可按仓库过滤
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(Stock::getWarehouseId, warehouseId);
        }
        List<Stock> stocks = list(wrapper);
        return stocks.stream().mapToInt(s -> s.getQuantity() == null ? 0 : s.getQuantity()).sum();
    }

    @Override
    public Long productCategoryCount(Long warehouseId) {
        Long count = baseMapper.countProductCategory(warehouseId);
        return count == null ? 0L : count;
    }
}
