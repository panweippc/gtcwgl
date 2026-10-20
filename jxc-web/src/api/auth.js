// 认证相关 API
import request from '@/utils/request'

/** 获取验证码:返回 captchaKey + captchaImg(base64) */
export function getCaptcha() {
  return request({ url: '/auth/captcha', method: 'get' })
}

/** 登录 */
export function login(data) {
  return request({ url: '/auth/login', method: 'post', data })
}

/** 获取当前登录用户信息 */
export function getUserInfo() {
  return request({ url: '/auth/info', method: 'get' })
}

/** 退出登录 */
export function logout() {
  return request({ url: '/auth/logout', method: 'post' })
}
