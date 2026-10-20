package com.jxc.module.supplier.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.common.PageResult;
import com.jxc.module.supplier.entity.Supplier;

import java.util.List;

/**
 * 供应商服务接口
 *
 * @author jxc
 */
public interface SupplierService extends IService<Supplier> {

    /**
     * 分页查询(多条件)
     *
     * @param page         页码
     * @param size         每页大小
     * @param supplierName 供应商名称
     * @param supplierCode 供应商编码
     * @param categoryId   分类ID
     * @param status       状态
     * @return 分页结果
     */
    PageResult<Supplier> page(Long page, Long size, String supplierName, String supplierCode, Long categoryId, Integer status);

    /**
     * 查询全部启用供应商
     *
     * @return 供应商列表
     */
    List<Supplier> listAllEnabled();

    /**
     * 删除供应商(校验是否存在采购/采购退货关联记录)
     *
     * @param id 供应商ID
     */
    void deleteWithCheck(Long id);
}
