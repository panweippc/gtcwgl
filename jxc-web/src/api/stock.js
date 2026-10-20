// 库存 API: 查询 / 预警 / 实时数量 / 调拨单 CRUD + 审核
import request from '@/utils/request'

/** 分页查询库存 params: { page, size, warehouseId, productId, productName, productCode } */
export function getStockPage(params) {
  return request({ url: '/stock/page', method: 'get', params })
}

/** 库存预警列表 warehouseId 可选 */
export function getStockWarning(warehouseId) {
  return request({ url: '/stock/warning', method: 'get', params: { warehouseId } })
}

/** 库存预警分页查询 params: { page, size, warehouseId } */
export function getStockWarningPage(params) {
  return request({ url: '/stock/warning/page', method: 'get', params })
}

/** 查询指定仓库指定商品当前库存数量 */
export function getStockQuantity(warehouseId, productId) {
  return request({ url: '/stock/quantity', method: 'get', params: { warehouseId, productId } })
}

/** 分页查询某商品的库存变动流水 params: { page, size, productId } */
export function getStockFlow(params) {
  return request({ url: '/stock/flow', method: 'get', params })
}

/** 全部仓库所有商品的库存数量总和 params: { warehouseId } warehouseId可空 */
export function getStockTotal(params) {
  return request({ url: '/stock/total', method: 'get', params })
}

/** 统计去重商品种类数 params: { warehouseId } warehouseId可空 */
export function getProductCategoryCount(params) {
  return request({ url: '/stock/product-category-count', method: 'get', params })
}

/** 分页查询调拨单 params: { page, size, orderNo, fromWarehouseId, toWarehouseId, auditStatus } */
export function getStockTransferPage(params) {
  return request({ url: '/stock/transfer/page', method: 'get', params })
}

/** 导出调拨单(全部匹配条件,含明细),返回 List */
export function exportStockTransfers(params) {
  return request({ url: '/stock/transfer/export', method: 'get', params })
}

/** 查询调拨单详情(含明细 details[]) */
export function getStockTransfer(id) {
  return request({ url: '/stock/transfer/' + id, method: 'get' })
}

/** 新增调拨单 data: StockTransfer{fromWarehouseId,toWarehouseId,orderTime,remark,details:[...]} */
export function addStockTransfer(data) {
  return request({ url: '/stock/transfer', method: 'post', data })
}

/** 修改调拨单(审核后不可改) */
export function updateStockTransfer(data) {
  return request({ url: '/stock/transfer', method: 'put', data })
}

/** 删除调拨单(审核后不可删) */
export function deleteStockTransfer(id) {
  return request({ url: '/stock/transfer/' + id, method: 'delete' })
}

/** 审核调拨单 data={remark}, 审核后扣减源仓库、增加目标仓库 */
export function auditStockTransfer(id, data) {
  return request({ url: '/stock/transfer/audit/' + id, method: 'post', data })
}

/** 分页查询盘点单 params: { page, size, orderNo, warehouseId, auditStatus } */
export function getStockCheckPage(params) {
  return request({ url: '/stock/check/page', method: 'get', params })
}

/** 导出盘点单(全部匹配条件,含明细),返回 List */
export function exportStockChecks(params) {
  return request({ url: '/stock/check/export', method: 'get', params })
}

/** 查询指定仓库账面库存列表(盘点自动带出) */
export function getStockCheckBookList(warehouseId) {
  return request({ url: '/stock/check/book-list', method: 'get', params: { warehouseId } })
}

/** 查询盘点单详情(含明细 details[]) */
export function getStockCheck(id) {
  return request({ url: '/stock/check/' + id, method: 'get' })
}

/** 新增盘点单 data: StockCheck{warehouseId,orderTime,remark,details:[...]} */
export function addStockCheck(data) {
  return request({ url: '/stock/check', method: 'post', data })
}

/** 修改盘点单(盘点后不可改) */
export function updateStockCheck(data) {
  return request({ url: '/stock/check', method: 'put', data })
}

/** 删除盘点单(盘点后不可删) */
export function deleteStockCheck(id) {
  return request({ url: '/stock/check/' + id, method: 'delete' })
}

/** 执行盘点 data={remark}, 生成盘盈/盘亏单并增减库存 */
export function auditStockCheck(id, data) {
  return request({ url: '/stock/check/audit/' + id, method: 'post', data })
}
