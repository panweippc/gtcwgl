package com.jxc.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.common.BusinessException;
import com.jxc.common.PageResult;
import com.jxc.module.product.entity.Product;
import com.jxc.module.product.mapper.ProductMapper;
import com.jxc.module.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商品服务实现
 * 核心规则:被采购/销售/退货/库存引用的商品禁止删除
 *
 * @author jxc
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public PageResult<Product> page(Long page, Long size, String productName, String productCode, Long categoryId, Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(productName), Product::getProductName, productName)
                .like(StringUtils.hasText(productCode), Product::getProductCode, productCode)
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .eq(status != null, Product::getStatus, status)
                .orderByDesc(Product::getCreateTime)
                .orderByDesc(Product::getId);
        IPage<Product> p = new Page<>(page, size);
        IPage<Product> result = page(p, wrapper);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public List<Product> listAllEnabled() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .orderByDesc(Product::getCreateTime)
                .orderByDesc(Product::getId);
        return list(wrapper);
    }

    @Override
    public void deleteWithCheck(Long id) {
        // 校验是否存在采购/销售/退货/库存关联数据,存在任意一种即禁止删除
        boolean hasRef = baseMapper.countPurchase(id) > 0
                || baseMapper.countSale(id) > 0
                || baseMapper.countPurchaseReturn(id) > 0
                || baseMapper.countSaleReturn(id) > 0
                || baseMapper.countStock(id) > 0;

        if (hasRef) {
            throw new BusinessException("该商品存在采购、销售记录,不可删除");
        }
        // 物理删除,避免逻辑删除 UPDATE 时触发 uk_product_code 唯一索引冲突
        baseMapper.physicalDelete(id);
    }
}
