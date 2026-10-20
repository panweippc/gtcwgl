package com.jxc.module.stock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.common.PageResult;
import com.jxc.module.stock.entity.StockCheck;
import com.jxc.module.stock.vo.StockBookVO;

import java.util.List;

/**
 * 库存盘点服务接口
 *
 * @author jxc
 */
public interface StockCheckService extends IService<StockCheck> {

    /**
     * 分页查询盘点单
     *
     * @param page        页码
     * @param size        每页大小
     * @param orderNo     盘点单编号
     * @param warehouseId 仓库ID
     * @param auditStatus 盘点状态
     * @return 分页结果
     */
    PageResult<StockCheck> page(Long page, Long size, String orderNo, Long warehouseId, Integer auditStatus);

    /**
     * 导出全部匹配条件的盘点单(含明细),供前端生成 Excel
     *
     * @param orderNo     盘点单编号
     * @param warehouseId 仓库ID
     * @param auditStatus 盘点状态
     * @return 盘点单列表(含明细)
     */
    List<StockCheck> export(String orderNo, Long warehouseId, Integer auditStatus);

    /**
     * 查询盘点单详情(含明细)
     *
     * @param id 主表ID
     * @return 盘点单
     */
    StockCheck getDetail(Long id);

    /**
     * 查询指定仓库的账面库存列表(盘点时自动带出)
     *
     * @param warehouseId 仓库ID
     * @return 账面库存列表
     */
    List<StockBookVO> bookList(Long warehouseId);

    /**
     * 新增盘点单(待盘点)
     *
     * @param check 盘点单
     * @return 是否成功
     */
    boolean create(StockCheck check);

    /**
     * 修改盘点单(仅待盘点可改)
     *
     * @param check 盘点单
     * @return 是否成功
     */
    boolean update(StockCheck check);

    /**
     * 删除盘点单(仅待盘点可删)
     *
     * @param id 主表ID
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 盘点执行(生成盘盈/盘亏单号, 盘盈增加库存、盘亏扣减库存, 盘点后不可改)
     *
     * @param id     主表ID
     * @param remark 盘点备注(可选)
     */
    void audit(Long id, String remark);
}
