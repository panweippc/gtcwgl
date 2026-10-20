package com.jxc.module.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.common.PageResult;
import com.jxc.module.purchase.entity.PurchaseReturnOrder;

import java.util.List;

/**
 * 采购退货服务接口
 *
 * @author jxc
 */
public interface PurchaseReturnOrderService extends IService<PurchaseReturnOrder> {

    /**
     * 分页查询
     */
    PageResult<PurchaseReturnOrder> page(Long page, Long size, String orderNo,
                                         Long supplierId, Long warehouseId, Integer auditStatus);

    /**
     * 导出:查询全部匹配条件的采购退货单(含明细)
     *
     * @param orderNo     退货单号
     * @param supplierId  供应商ID
     * @param warehouseId 仓库ID
     * @param auditStatus 审核状态
     * @return 退货单列表(含明细)
     */
    List<PurchaseReturnOrder> export(String orderNo, Long supplierId, Long warehouseId, Integer auditStatus);

    /**
     * 详情(含明细)
     */
    PurchaseReturnOrder getDetail(Long id);

    /**
     * 新增采购退货单(待审核)
     */
    boolean create(PurchaseReturnOrder order);

    /**
     * 修改采购退货单(仅待审核可改)
     */
    boolean update(PurchaseReturnOrder order);

    /**
     * 删除采购退货单(仅待审核可删)
     */
    boolean delete(Long id);

    /**
     * 审核采购退货单(扣减库存,审核后不可改)
     */
    void audit(Long id, String remark);
}
