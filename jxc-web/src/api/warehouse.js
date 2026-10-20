// 仓库 API
import request from '@/utils/request'

/** 分页查询仓库列表 */
export function getWarehousePage(params) {
  return request({ url: '/warehouse/page', method: 'get', params })
}

/** 查询全部启用仓库 */
export function getAllWarehouses() {
  return request({ url: '/warehouse/all', method: 'get' })
}

/** 根据 id 查询仓库 */
export function getWarehouse(id) {
  return request({ url: '/warehouse/' + id, method: 'get' })
}

/** 新增仓库 */
export function addWarehouse(data) {
  return request({ url: '/warehouse', method: 'post', data })
}

/** 修改仓库 */
export function updateWarehouse(data) {
  return request({ url: '/warehouse', method: 'put', data })
}

/** 删除仓库 */
export function deleteWarehouse(id) {
  return request({ url: '/warehouse/' + id, method: 'delete' })
}
