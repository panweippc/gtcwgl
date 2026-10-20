package com.jxc.module.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.common.PageResult;
import com.jxc.module.customer.entity.Customer;

import java.util.List;

/**
 * 客户服务接口
 *
 * @author jxc
 */
public interface CustomerService extends IService<Customer> {

    /**
     * 分页查询
     *
     * @param page         页码
     * @param size         每页大小
     * @param customerName 客户名称
     * @param customerCode 客户编码
     * @param categoryId   客户分类ID
     * @param status       状态:0-禁用,1-启用
     * @return 分页结果
     */
    PageResult<Customer> page(Long page, Long size, String customerName, String customerCode, Long categoryId, Integer status);

    /**
     * 查询全部启用客户
     *
     * @return 客户列表
     */
    List<Customer> listAllEnabled();

    /**
     * 根据ID查询客户
     *
     * @param id ID
     * @return 客户
     */
    Customer getCustomerById(Long id);

    /**
     * 删除客户(校验是否存在销售/销售退货关联记录)
     *
     * @param id 客户ID
     */
    void deleteWithCheck(Long id);
}
