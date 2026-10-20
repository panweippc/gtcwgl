package com.jxc.module.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.common.PageResult;
import com.jxc.module.product.entity.Product;

import java.util.List;

/**
 * 商品服务接口
 *
 * @author jxc
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询商品(多条件)
     *
     * @param page        页码
     * @param size        每页大小
     * @param productName 商品名称
     * @param productCode 商品编码
     * @param categoryId  分类ID
     * @param status      状态
     * @return 分页结果
     */
    PageResult<Product> page(Long page, Long size, String productName, String productCode, Long categoryId, Integer status);

    /**
     * 查询全部启用商品
     *
     * @return 商品列表
     */
    List<Product> listAllEnabled();

    /**
     * 删除商品(校验关联的采购/销售/退货/库存记录)
     *
     * @param id 商品ID
     */
    void deleteWithCheck(Long id);
}
