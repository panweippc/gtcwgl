// 商品与商品分类 API
import request from '@/utils/request'

// ===== 商品 =====

/** 分页查询商品列表 */
export function getProductPage(params) {
  return request({ url: '/product/page', method: 'get', params })
}

/** 查询全部启用商品 */
export function getAllProducts() {
  return request({ url: '/product/all', method: 'get' })
}

/** 根据 id 查询商品 */
export function getProduct(id) {
  return request({ url: '/product/' + id, method: 'get' })
}

/** 新增商品 */
export function addProduct(data) {
  return request({ url: '/product', method: 'post', data })
}

/** 修改商品 */
export function updateProduct(data) {
  return request({ url: '/product', method: 'put', data })
}

/** 删除商品 */
export function deleteProduct(id) {
  return request({ url: '/product/' + id, method: 'delete' })
}

// ===== 商品分类 =====

/** 查询商品分类树 */
export function getProductCategoryTree() {
  return request({ url: '/product/category/tree', method: 'get' })
}

/** 全部分类树(含禁用),供分类管理列表展示使用 */
export function getProductCategoryTreeAll() {
  return request({ url: '/product/category/treeAll', method: 'get' })
}

/** 查询全部启用商品分类 */
export function getAllProductCategories() {
  return request({ url: '/product/category/all', method: 'get' })
}

/** 根据 id 查询商品分类 */
export function getProductCategory(id) {
  return request({ url: '/product/category/' + id, method: 'get' })
}

/** 新增商品分类 */
export function addProductCategory(data) {
  return request({ url: '/product/category', method: 'post', data })
}

/** 修改商品分类 */
export function updateProductCategory(data) {
  return request({ url: '/product/category', method: 'put', data })
}

/** 删除商品分类 */
export function deleteProductCategory(id) {
  return request({ url: '/product/category/' + id, method: 'delete' })
}
