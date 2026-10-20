package com.jxc.module.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.common.PageResult;
import com.jxc.module.purchase.entity.PurchaseOrder;

import java.util.List;

/**
 * 采购入库服务接口
 *
 * @author jxc
 */
public interface PurchaseOrderService extends IService<PurchaseOrder> {

    /**
     * 分页查询
     *
     * @param page        页码
     * @param size        每页大小
     * @param orderNo     单号
     * @param supplierId  供应商ID
     * @param warehouseId 仓库ID
     * @param auditStatus 审核状态
     * @return 分页
     */
    PageResult<PurchaseOrder> page(Long page, Long size, String orderNo,
                                  Long supplierId, Long warehouseId, Integer auditStatus);

    /**
     * 导出:查询全部匹配条件的采购单(含明细)
     *
     * @param orderNo     单号
     * @param supplierId  供应商ID
     * @param warehouseId 仓库ID
     * @param auditStatus 审核状态
     * @return 采购单列表(含明细)
     */
    List<PurchaseOrder> export(String orderNo, Long supplierId, Long warehouseId, Integer auditStatus);

    /**
     * 详情(含明细)
     *
     * @param id 主表ID
     * @return 采购单
     */
    PurchaseOrder getDetail(Long id);

    /**
     * 新增采购单(待审核)
     *
     * @param order 采购单
     * @return 是否成功
     */
    boolean create(PurchaseOrder order);

    /**
     * 修改采购单(仅待审核可改)
     *
     * @param order 采购单
     * @return 是否成功
     */
    boolean update(PurchaseOrder order);

    /**
     * 删除采购单(仅待审核可删)
     *
     * @param id 主表ID
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 审核采购单(增加库存,审核后不可改)
     *
     * @param id     主表ID
     * @param remark 审核备注
     */
    void audit(Long id, String remark);
}
