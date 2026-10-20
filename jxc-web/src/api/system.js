// 系统管理 API: 用户 / 角色 / 菜单 / 操作日志
import request from '@/utils/request'

// ======================== 用户 ========================

/** 分页查询用户列表 */
export function getUserPage(params) {
  return request({ url: '/system/user/page', method: 'get', params })
}

/** 查询用户详情 */
export function getUser(id) {
  return request({ url: '/system/user/' + id, method: 'get' })
}

/** 新增用户 data 为 SysUser, password 为初始密码 */
export function addUser(data, password) {
  return request({ url: '/system/user', method: 'post', data, params: { password } })
}

/** 修改用户 */
export function updateUser(data) {
  return request({ url: '/system/user', method: 'put', data })
}

/** 删除用户 */
export function deleteUser(id) {
  return request({ url: '/system/user/' + id, method: 'delete' })
}

/** 重置密码 data={userId, password} */
export function resetPassword(data) {
  return request({ url: '/system/user/reset-password', method: 'post', data })
}

/** 修改密码 data={oldPassword, newPassword} */
export function changePassword(data) {
  return request({ url: '/system/user/change-password', method: 'post', data })
}

/** 切换用户状态 status=0禁用 1启用 */
export function toggleUserStatus(id, status) {
  return request({ url: '/system/user/status/' + id, method: 'post', params: { status } })
}

/** 获取当前登录用户个人信息 */
export function getProfile() {
  return request({ url: '/system/user/profile', method: 'get' })
}

/** 更新当前登录用户个人信息 data={realName, phone, email, remark} */
export function updateProfile(data) {
  return request({ url: '/system/user/profile', method: 'put', data })
}

// ======================== 角色 ========================

/** 分页查询角色列表 */
export function getRolePage(params) {
  return request({ url: '/system/role/page', method: 'get', params })
}

/** 查询全部启用角色 */
export function getAllRoles() {
  return request({ url: '/system/role/all', method: 'get' })
}

/** 查询角色详情 */
export function getRole(id) {
  return request({ url: '/system/role/' + id, method: 'get' })
}

/** 新增角色 */
export function addRole(data) {
  return request({ url: '/system/role', method: 'post', data })
}

/** 修改角色 */
export function updateRole(data) {
  return request({ url: '/system/role', method: 'put', data })
}

/** 删除角色 */
export function deleteRole(id) {
  return request({ url: '/system/role/' + id, method: 'delete' })
}

/** 给角色分配菜单 data={roleId, menuIds:[...]} */
export function assignMenus(data) {
  return request({ url: '/system/role/assign-menus', method: 'post', data })
}

/** 查询角色已分配菜单 ID 列表 */
export function getRoleMenus(roleId) {
  return request({ url: '/system/role/menus/' + roleId, method: 'get' })
}

// ======================== 菜单 ========================

/** 查询菜单树 */
export function getMenuTree() {
  return request({ url: '/system/menu/tree', method: 'get' })
}

/** 查询菜单详情 */
export function getMenu(id) {
  return request({ url: '/system/menu/' + id, method: 'get' })
}

/** 新增菜单 */
export function addMenu(data) {
  return request({ url: '/system/menu', method: 'post', data })
}

/** 修改菜单 */
export function updateMenu(data) {
  return request({ url: '/system/menu', method: 'put', data })
}

/** 删除菜单 */
export function deleteMenu(id) {
  return request({ url: '/system/menu/' + id, method: 'delete' })
}

// ======================== 操作日志 ========================

/** 分页查询操作日志 */
export function getLogPage(params) {
  return request({ url: '/system/log/page', method: 'get', params })
}

/** 删除单条日志 */
export function deleteLog(id) {
  return request({ url: '/system/log/' + id, method: 'delete' })
}

/** 清空全部日志 */
export function clearLog() {
  return request({ url: '/system/log/clear', method: 'delete' })
}
