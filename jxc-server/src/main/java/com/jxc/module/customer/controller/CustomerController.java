package com.jxc.module.customer.controller;

import com.jxc.common.PageResult;
import com.jxc.common.Result;
import com.jxc.module.customer.entity.Customer;
import com.jxc.module.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 客户控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<PageResult<Customer>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return Result.success(customerService.page(page, size, customerName, customerCode, categoryId, status));
    }

    /**
     * 查询全部启用客户
     */
    @GetMapping("/all")
    public Result<List<Customer>> all() {
        return Result.success(customerService.listAllEnabled());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public Result<Customer> get(@PathVariable Long id) {
        return Result.success(customerService.getCustomerById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Void> add(@RequestBody Customer customer) {
        customerService.save(customer);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping
    public Result<Void> update(@RequestBody Customer customer) {
        customerService.updateById(customer);
        return Result.success();
    }

    /**
     * 删除(校验是否存在销售关联记录)
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.deleteWithCheck(id);
        return Result.success();
    }
}
