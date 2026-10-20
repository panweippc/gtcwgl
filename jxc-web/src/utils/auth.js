// 本地存储 token / userInfo 工具
const TOKEN_KEY = 'jxc_token'
const USER_INFO_KEY = 'jxc_user_info'

/** 获取 token */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

/** 设置 token */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token || '')
}

/** 清除 token */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

/** 获取缓存的用户信息 */
export function getUserInfo() {
  const raw = localStorage.getItem(USER_INFO_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

/** 缓存用户信息 */
export function setUserInfo(info) {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(info || {}))
}

/** 清除用户信息 */
export function removeUserInfo() {
  localStorage.removeItem(USER_INFO_KEY)
}
