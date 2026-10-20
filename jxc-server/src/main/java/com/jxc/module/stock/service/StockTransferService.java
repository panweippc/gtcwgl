package com.jxc.module.stock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.common.PageResult;
import com.jxc.module.stock.entity.StockTransfer;

import java.util.List;

/**
 * 库存调拨服务接口
 *
 * @author jxc
 */
public interface StockTransferService extends IService<StockTransfer> {

    /**
     * 分页查询调拨单
     *
     * @param page        页码
     * @param size        每页大小
     * @param orderNo     单号
     * @param fromWarehouseId 调出仓库
     * @param toWarehouseId   调入仓库
     * @param auditStatus 审核状态
     * @return 分页
     */
    PageResult<StockTransfer> page(Long page, Long size, String orderNo,
                                  Long fromWarehouseId, Long toWarehouseId, Integer auditStatus);

    /**
     * 导出全部匹配条件的调拨单(含明细),供前端生成 Excel
     *
     * @param orderNo        单号
     * @param fromWarehouseId 调出仓库
     * @param toWarehouseId   调入仓库
     * @param auditStatus    审核状态
     * @return 调拨单列表(含明细)
     */
    List<StockTransfer> export(String orderNo, Long fromWarehouseId, Long toWarehouseId, Integer auditStatus);

    /**
     * 查询调拨单详情(含明细)
     *
     * @param id 主表ID
     * @return 调拨单
     */
    StockTransfer getDetail(Long id);

    /**
     * 新增调拨单(待审核)
     *
     * @param transfer 调拨单
     * @return 是否成功
     */
    boolean create(StockTransfer transfer);

    /**
     * 修改调拨单(仅待审核可改)
     *
     * @param transfer 调拨单
     * @return 是否成功
     */
    boolean update(StockTransfer transfer);

    /**
     * 删除调拨单(仅待审核可删)
     *
     * @param id 主表ID
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 审核调拨单(执行库存调拨,审核后不可改)
     *
     * @param id       主表ID
     * @param remark   审核备注(可选)
     */
    void audit(Long id, String remark);
}
