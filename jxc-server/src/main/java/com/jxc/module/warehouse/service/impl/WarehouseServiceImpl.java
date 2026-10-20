package com.jxc.module.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.common.BusinessException;
import com.jxc.common.PageResult;
import com.jxc.module.warehouse.entity.Warehouse;
import com.jxc.module.warehouse.mapper.WarehouseMapper;
import com.jxc.module.warehouse.service.WarehouseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 仓库服务实现
 *
 * @author jxc
 */
@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    @Override
    public PageResult<Warehouse> page(Long page, Long size, String warehouseName, String warehouseCode, Integer status) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(warehouseName), Warehouse::getWarehouseName, warehouseName)
                .like(StringUtils.hasText(warehouseCode), Warehouse::getWarehouseCode, warehouseCode)
                .eq(status != null, Warehouse::getStatus, status)
                .orderByAsc(Warehouse::getSort);
        IPage<Warehouse> p = new Page<>(page, size);
        IPage<Warehouse> result = page(p, wrapper);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public List<Warehouse> listAllEnabled() {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Warehouse::getStatus, 1).orderByAsc(Warehouse::getSort);
        return list(wrapper);
    }

    @Override
    public Warehouse getWarehouseById(Long id) {
        return getById(id);
    }

    @Override
    public void deleteWithCheck(Long id) {
        // 校验该仓库是否存在库存记录(库存数量大于0),存在则禁止删除
        if (baseMapper.countStock(id) > 0) {
            throw new BusinessException("该仓库存在商品库存,不可删除");
        }
        removeById(id);
    }
}
