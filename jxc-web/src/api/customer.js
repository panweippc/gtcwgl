// 客户与客户分类 API
import request from '@/utils/request'

// ===== 客户 =====

/** 分页查询客户列表 */
export function getCustomerPage(params) {
  return request({ url: '/customer/page', method: 'get', params })
}

/** 查询全部启用客户 */
export function getAllCustomers() {
  return request({ url: '/customer/all', method: 'get' })
}

/** 根据 id 查询客户 */
export function getCustomer(id) {
  return request({ url: '/customer/' + id, method: 'get' })
}

/** 新增客户 */
export function addCustomer(data) {
  return request({ url: '/customer', method: 'post', data })
}

/** 修改客户 */
export function updateCustomer(data) {
  return request({ url: '/customer', method: 'put', data })
}

/** 删除客户 */
export function deleteCustomer(id) {
  return request({ url: '/customer/' + id, method: 'delete' })
}

// ===== 客户分类 =====

/** 查询客户分类树 */
export function getCustomerCategoryTree() {
  return request({ url: '/customer/category/tree', method: 'get' })
}

/** 全部分类树(含禁用),供分类管理列表展示使用 */
export function getCustomerCategoryTreeAll() {
  return request({ url: '/customer/category/treeAll', method: 'get' })
}

/** 查询全部启用客户分类 */
export function getAllCustomerCategories() {
  return request({ url: '/customer/category/all', method: 'get' })
}

/** 根据 id 查询客户分类 */
export function getCustomerCategory(id) {
  return request({ url: '/customer/category/' + id, method: 'get' })
}

/** 新增客户分类 */
export function addCustomerCategory(data) {
  return request({ url: '/customer/category', method: 'post', data })
}

/** 修改客户分类 */
export function updateCustomerCategory(data) {
  return request({ url: '/customer/category', method: 'put', data })
}

/** 删除客户分类 */
export function deleteCustomerCategory(id) {
  return request({ url: '/customer/category/' + id, method: 'delete' })
}
