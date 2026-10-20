// 数据统计 API
import request from '@/utils/request'

/** 首页看板 */
export function getDashboard() {
  return request({ url: '/stats/dashboard', method: 'get' })
}

/** 按日采购统计 startDate, endDate (yyyy-MM-dd) */
export function getDailyPurchase(params) {
  return request({ url: '/stats/purchase/daily', method: 'get', params })
}

/** 采购汇总(采购/退货总数量、总金额) startDate, endDate (yyyy-MM-dd) */
export function getPurchaseSummary(params) {
  return request({ url: '/stats/purchase/summary', method: 'get', params })
}

/** 按日采购退货统计 startDate, endDate (yyyy-MM-dd) */
export function getDailyPurchaseReturn(params) {
  return request({ url: '/stats/purchase-return/daily', method: 'get', params })
}

/** 按日销售统计 startDate, endDate (yyyy-MM-dd) */
export function getDailySale(params) {
  return request({ url: '/stats/sale/daily', method: 'get', params })
}

/** 销售汇总(销售/退货总数量、总金额) startDate, endDate (yyyy-MM-dd) */
export function getSaleSummary(params) {
  return request({ url: '/stats/sale/summary', method: 'get', params })
}

/** 按日销售退货统计 startDate, endDate (yyyy-MM-dd) */
export function getDailySaleReturn(params) {
  return request({ url: '/stats/sale-return/daily', method: 'get', params })
}

/** 按仓库库存统计 */
export function getStockByWarehouse() {
  return request({ url: '/stats/stock/warehouse', method: 'get' })
}
