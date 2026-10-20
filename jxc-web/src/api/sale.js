// 销售单 API: 出库 + 退货
import request from '@/utils/request'

/* ========== 销售出库 ========== */

/** 分页查询销售出库单 params: { page, size, orderNo, customerId, warehouseId, auditStatus } */
export function getSaleOrderPage(params) {
  return request({ url: '/sale/order/page', method: 'get', params })
}

/** 查询销售出库单详情(含明细 details[]) */
export function getSaleOrder(id) {
  return request({ url: '/sale/order/' + id, method: 'get' })
}

/** 新增销售出库单 data: SaleOrder{customerId,warehouseId,orderTime,operatorId,remark,details:[...]} */
export function addSaleOrder(data) {
  return request({ url: '/sale/order', method: 'post', data })
}

/** 修改销售出库单(审核后不可改) */
export function updateSaleOrder(data) {
  return request({ url: '/sale/order', method: 'put', data })
}

/** 删除销售出库单(审核后不可删) */
export function deleteSaleOrder(id) {
  return request({ url: '/sale/order/' + id, method: 'delete' })
}

/** 审核销售出库单 data={remark}, 审核后扣减库存(禁止负库存) */
export function auditSaleOrder(id, data) {
  return request({ url: '/sale/order/audit/' + id, method: 'post', data })
}

/** 导出销售出库单(全部匹配条件,含明细),返回 List */
export function exportSaleOrders(params) {
  return request({ url: '/sale/order/export', method: 'get', params })
}

/* ========== 销售退货 ========== */

/** 分页查询销售退货单 */
export function getSaleReturnPage(params) {
  return request({ url: '/sale/return/page', method: 'get', params })
}

/** 查询销售退货单详情 */
export function getSaleReturn(id) {
  return request({ url: '/sale/return/' + id, method: 'get' })
}

/** 新增销售退货单 */
export function addSaleReturn(data) {
  return request({ url: '/sale/return', method: 'post', data })
}

/** 修改销售退货单(审核后不可改) */
export function updateSaleReturn(data) {
  return request({ url: '/sale/return', method: 'put', data })
}

/** 删除销售退货单(审核后不可删) */
export function deleteSaleReturn(id) {
  return request({ url: '/sale/return/' + id, method: 'delete' })
}

/** 审核销售退货单 data={remark}, 审核后增加库存 */
export function auditSaleReturn(id, data) {
  return request({ url: '/sale/return/audit/' + id, method: 'post', data })
}

/** 导出销售退货单(全部匹配条件,含明细),返回 List */
export function exportSaleReturns(params) {
  return request({ url: '/sale/return/export', method: 'get', params })
}
