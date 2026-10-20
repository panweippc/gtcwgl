// 采购单 API: 入库 + 退货
import request from '@/utils/request'

/* ========== 采购入库 ========== */

/** 分页查询采购入库单 params: { page, size, orderNo, supplierId, warehouseId, auditStatus } */
export function getPurchaseOrderPage(params) {
  return request({ url: '/purchase/order/page', method: 'get', params })
}

/** 导出采购入库单(全部匹配条件,含明细),返回 List */
export function exportPurchaseOrders(params) {
  return request({ url: '/purchase/order/export', method: 'get', params })
}

/** 查询采购入库单详情(含明细 details[]) */
export function getPurchaseOrder(id) {
  return request({ url: '/purchase/order/' + id, method: 'get' })
}

/** 新增采购入库单 data: PurchaseOrder{supplierId,warehouseId,orderTime,operatorId,remark,details:[...]} */
export function addPurchaseOrder(data) {
  return request({ url: '/purchase/order', method: 'post', data })
}

/** 修改采购入库单(审核后不可改) */
export function updatePurchaseOrder(data) {
  return request({ url: '/purchase/order', method: 'put', data })
}

/** 删除采购入库单(审核后不可删) */
export function deletePurchaseOrder(id) {
  return request({ url: '/purchase/order/' + id, method: 'delete' })
}

/** 审核采购入库单 data={remark}, 审核后增加库存 */
export function auditPurchaseOrder(id, data) {
  return request({ url: '/purchase/order/audit/' + id, method: 'post', data })
}

/* ========== 采购退货 ========== */

/** 分页查询采购退货单 */
export function getPurchaseReturnPage(params) {
  return request({ url: '/purchase/return/page', method: 'get', params })
}

/** 导出采购退货单(全部匹配条件,含明细),返回 List */
export function exportPurchaseReturns(params) {
  return request({ url: '/purchase/return/export', method: 'get', params })
}

/** 查询采购退货单详情 */
export function getPurchaseReturn(id) {
  return request({ url: '/purchase/return/' + id, method: 'get' })
}

/** 新增采购退货单 */
export function addPurchaseReturn(data) {
  return request({ url: '/purchase/return', method: 'post', data })
}

/** 修改采购退货单(审核后不可改) */
export function updatePurchaseReturn(data) {
  return request({ url: '/purchase/return', method: 'put', data })
}

/** 删除采购退货单(审核后不可删) */
export function deletePurchaseReturn(id) {
  return request({ url: '/purchase/return/' + id, method: 'delete' })
}

/** 审核采购退货单 data={remark}, 审核后扣减库存 */
export function auditPurchaseReturn(id, data) {
  return request({ url: '/purchase/return/audit/' + id, method: 'post', data })
}
