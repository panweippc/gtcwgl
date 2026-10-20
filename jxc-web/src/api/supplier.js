// 供应商与供应商分类 API
import request from '@/utils/request'

// ===== 供应商 =====

/** 分页查询供应商列表 */
export function getSupplierPage(params) {
  return request({ url: '/supplier/page', method: 'get', params })
}

/** 查询全部启用供应商 */
export function getAllSuppliers() {
  return request({ url: '/supplier/all', method: 'get' })
}

/** 根据 id 查询供应商 */
export function getSupplier(id) {
  return request({ url: '/supplier/' + id, method: 'get' })
}

/** 新增供应商 */
export function addSupplier(data) {
  return request({ url: '/supplier', method: 'post', data })
}

/** 修改供应商 */
export function updateSupplier(data) {
  return request({ url: '/supplier', method: 'put', data })
}

/** 删除供应商 */
export function deleteSupplier(id) {
  return request({ url: '/supplier/' + id, method: 'delete' })
}

// ===== 供应商分类 =====

/** 查询供应商分类树 */
export function getSupplierCategoryTree() {
  return request({ url: '/supplier/category/tree', method: 'get' })
}

/** 全部分类树(含禁用),供分类管理列表展示使用 */
export function getSupplierCategoryTreeAll() {
  return request({ url: '/supplier/category/treeAll', method: 'get' })
}

/** 查询全部启用供应商分类 */
export function getAllSupplierCategories() {
  return request({ url: '/supplier/category/all', method: 'get' })
}

/** 根据 id 查询供应商分类 */
export function getSupplierCategory(id) {
  return request({ url: '/supplier/category/' + id, method: 'get' })
}

/** 新增供应商分类 */
export function addSupplierCategory(data) {
  return request({ url: '/supplier/category', method: 'post', data })
}

/** 修改供应商分类 */
export function updateSupplierCategory(data) {
  return request({ url: '/supplier/category', method: 'put', data })
}

/** 删除供应商分类 */
export function deleteSupplierCategory(id) {
  return request({ url: '/supplier/category/' + id, method: 'delete' })
}
