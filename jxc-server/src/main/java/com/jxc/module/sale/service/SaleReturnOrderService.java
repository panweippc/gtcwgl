package com.jxc.module.sale.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.common.PageResult;
import com.jxc.module.sale.entity.SaleReturnOrder;

import java.util.List;

/**
 * 销售退货服务接口
 *
 * @author jxc
 */
public interface SaleReturnOrderService extends IService<SaleReturnOrder> {

    /**
     * 分页查询
     */
    PageResult<SaleReturnOrder> page(Long page, Long size, String orderNo,
                                     Long customerId, Long warehouseId, Integer auditStatus);

    /**
     * 详情(含明细)
     */
    SaleReturnOrder getDetail(Long id);

    /**
     * 新增销售退货单(待审核)
     */
    boolean create(SaleReturnOrder order);

    /**
     * 修改销售退货单(仅待审核可改)
     */
    boolean update(SaleReturnOrder order);

    /**
     * 删除销售退货单(仅待审核可删)
     */
    boolean delete(Long id);

    /**
     * 审核销售退货单(增加库存,审核后不可改)
     */
    void audit(Long id, String remark);

    /**
     * 导出:查询全部匹配条件的销售退货单(含明细)
     */
    List<SaleReturnOrder> export(String orderNo, Long customerId, Long warehouseId, Integer auditStatus);
}
