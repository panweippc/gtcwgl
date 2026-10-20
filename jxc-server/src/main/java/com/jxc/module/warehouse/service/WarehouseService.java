package com.jxc.module.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.common.PageResult;
import com.jxc.module.warehouse.entity.Warehouse;

import java.util.List;

/**
 * 仓库服务接口
 *
 * @author jxc
 */
public interface WarehouseService extends IService<Warehouse> {

    /**
     * 分页查询
     *
     * @param page          页码
     * @param size          每页大小
     * @param warehouseName 仓库名称
     * @param warehouseCode 仓库编码
     * @param status        状态:0-禁用,1-启用
     * @return 分页结果
     */
    PageResult<Warehouse> page(Long page, Long size, String warehouseName, String warehouseCode, Integer status);

    /**
     * 查询全部启用仓库
     *
     * @return 仓库列表
     */
    List<Warehouse> listAllEnabled();

    /**
     * 根据ID查询仓库
     *
     * @param id ID
     * @return 仓库
     */
    Warehouse getWarehouseById(Long id);

    /**
     * 删除仓库(校验是否存在库存记录)
     *
     * @param id 仓库ID
     */
    void deleteWithCheck(Long id);
}
