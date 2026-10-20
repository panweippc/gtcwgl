package com.jxc.module.stock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxc.common.PageResult;
import com.jxc.module.stock.entity.Stock;
import com.jxc.module.stock.vo.StockFlowVO;
import com.jxc.module.stock.vo.StockWarningVO;

import java.util.List;

/**
 * 库存服务接口
 *
 * @author jxc
 */
public interface StockService extends IService<Stock> {

    /**
     * 分页查询库存(联表仓库/商品)
     *
     * @param page        页码
     * @param size        每页大小
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @param productName 商品名称
     * @param productCode 商品编码
     * @return 分页结果
     */
    PageResult<StockWarningVO> page(Long page, Long size, Long warehouseId,
                                   Long productId, String productName, String productCode);

    /**
     * 增加库存(用于采购入库审核)
     * 不存在则自动创建
     *
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @param quantity    增加数量(正数)
     */
    void addStock(Long warehouseId, Long productId, Integer quantity);

    /**
     * 扣减库存(用于销售出库审核)
     * 库存不足抛出业务异常,禁止负库存
     *
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @param quantity    扣减数量(正数)
     */
    void deductStock(Long warehouseId, Long productId, Integer quantity);

    /**
     * 库存调拨(调出仓库扣减,调入仓库增加)
     * 同一事务内执行,失败回滚
     *
     * @param fromWarehouseId 调出仓库ID
     * @param toWarehouseId   调入仓库ID
     * @param productId       商品ID
     * @param quantity        调拨数量(正数)
     */
    void transferStock(Long fromWarehouseId, Long toWarehouseId, Long productId, Integer quantity);

    /**
     * 查询库存预警列表
     *
     * @param warehouseId 仓库ID(可空)
     * @return 预警列表
     */
    List<StockWarningVO> warningList(Long warehouseId);

    /**
     * 分页查询库存预警
     *
     * @param page        页码
     * @param size        每页大小
     * @param warehouseId 仓库ID(可空)
     * @param warningType 预警类型(可空:1-不足,2-积压)
     * @return 分页结果
     */
    PageResult<StockWarningVO> warningPage(Long page, Long size, Long warehouseId, Integer warningType);

    /**
     * 查询某仓库某商品的当前库存数量
     *
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @return 库存数量(不存在返回0)
     */
    Integer getQuantity(Long warehouseId, Long productId);

    /**
     * 分页查询某商品的库存变动流水(采购入库、采购退货、销售出库、销售退货、调拨)
     *
     * @param page      页码
     * @param size      每页大小
     * @param productId 商品ID
     * @return 流水分页结果
     */
    PageResult<StockFlowVO> flowPage(Long page, Long size, Long productId);

    /**
     * 全部仓库所有商品的库存数量总和
     *
     * @param warehouseId 仓库ID(可选,为null时统计全部仓库)
     * @return 库存总数
     */
    Integer totalStock(Long warehouseId);

    /**
     * 统计去重商品种类数
     *
     * @param warehouseId 仓库ID(可选,为null时统计全部仓库)
     * @return 商品种类数
     */
    Long productCategoryCount(Long warehouseId);
}
