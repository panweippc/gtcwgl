package com.jxc.module.supplier.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.common.BusinessException;
import com.jxc.common.PageResult;
import com.jxc.module.supplier.entity.Supplier;
import com.jxc.module.supplier.mapper.SupplierMapper;
import com.jxc.module.supplier.service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 供应商服务实现
 *
 * @author jxc
 */
@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {

    @Override
    public PageResult<Supplier> page(Long page, Long size, String supplierName, String supplierCode, Long categoryId, Integer status) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(supplierName), Supplier::getSupplierName, supplierName)
                .like(StringUtils.hasText(supplierCode), Supplier::getSupplierCode, supplierCode)
                .eq(categoryId != null, Supplier::getCategoryId, categoryId)
                .eq(status != null, Supplier::getStatus, status)
                .orderByDesc(Supplier::getCreateTime);
        IPage<Supplier> p = new Page<>(page, size);
        IPage<Supplier> result = page(p, wrapper);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public List<Supplier> listAllEnabled() {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Supplier::getStatus, 1)
                .orderByDesc(Supplier::getCreateTime);
        return list(wrapper);
    }

    @Override
    public void deleteWithCheck(Long id) {
        // 校验是否存在采购入库或采购退货关联记录,存在任意一种即禁止删除
        boolean hasPurchase = baseMapper.countPurchase(id) > 0
                || baseMapper.countPurchaseReturn(id) > 0;
        if (hasPurchase) {
            throw new BusinessException("该供应商存在采购记录,不可删除");
        }
        removeById(id);
    }
}
