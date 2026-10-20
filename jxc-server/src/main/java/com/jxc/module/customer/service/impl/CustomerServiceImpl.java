package com.jxc.module.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.common.BusinessException;
import com.jxc.common.PageResult;
import com.jxc.module.customer.entity.Customer;
import com.jxc.module.customer.mapper.CustomerMapper;
import com.jxc.module.customer.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 客户服务实现
 *
 * @author jxc
 */
@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Override
    public PageResult<Customer> page(Long page, Long size, String customerName, String customerCode, Long categoryId, Integer status) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(customerName), Customer::getCustomerName, customerName)
                .like(StringUtils.hasText(customerCode), Customer::getCustomerCode, customerCode)
                .eq(categoryId != null, Customer::getCategoryId, categoryId)
                .eq(status != null, Customer::getStatus, status)
                .orderByDesc(Customer::getId);
        IPage<Customer> p = new Page<>(page, size);
        IPage<Customer> result = page(p, wrapper);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public List<Customer> listAllEnabled() {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getStatus, 1).orderByDesc(Customer::getId);
        return list(wrapper);
    }

    @Override
    public Customer getCustomerById(Long id) {
        return getById(id);
    }

    @Override
    public void deleteWithCheck(Long id) {
        // 校验是否存在销售出库或销售退货关联记录,存在任意一种即禁止删除
        boolean hasSale = baseMapper.countSale(id) > 0
                || baseMapper.countSaleReturn(id) > 0;
        if (hasSale) {
            throw new BusinessException("该客户存在销售记录,不可删除");
        }
        removeById(id);
    }
}
