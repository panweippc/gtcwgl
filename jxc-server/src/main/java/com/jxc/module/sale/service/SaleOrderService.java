package com.jxc.module.sale.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.common.PageResult;
import com.jxc.module.sale.entity.SaleOrder;

import java.util.List;

/**
 * 销售出库服务接口
 *
 * @author jxc
 */
public interface SaleOrderService extends IService<SaleOrder> {

    /**
     * 分页查询
     *
     * @param page        页码
     * @param size        每页大小
     * @param orderNo     单号
     * @param customerId  客户ID
     * @param warehouseId 仓库ID
     * @param auditStatus 审核状态
     * @return 分页
     */
    PageResult<SaleOrder> page(Long page, Long size, String orderNo,
                              Long customerId, Long warehouseId, Integer auditStatus);

    /**
     * 详情(含明细)
     *
     * @param id 主表ID
     * @return 销售单
     */
    SaleOrder getDetail(Long id);

    /**
     * 新增销售单(待审核)
     *
     * @param order 销售单
     * @return 是否成功
     */
    boolean create(SaleOrder order);

    /**
     * 修改销售单(仅待审核可改)
     *
     * @param order 销售单
     * @return 是否成功
     */
    boolean update(SaleOrder order);

    /**
     * 删除销售单(仅待审核可删)
     *
     * @param id 主表ID
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 审核销售单(扣减库存,审核后不可改)
     *
     * @param id     主表ID
     * @param remark 审核备注
     */
    void audit(Long id, String remark);

    /**
     * 导出:查询全部匹配条件的销售出库单(含明细)
     */
    List<SaleOrder> export(String orderNo, Long customerId, Long warehouseId, Integer auditStatus);
}
