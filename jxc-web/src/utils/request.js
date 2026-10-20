// Axios 实例: 统一注入 token、解包 Result{code,msg,data}
import axios from 'axios'
import { Message, MessageBox } from 'element-ui'
import { getToken, removeToken, removeUserInfo } from './auth'

const service = axios.create({
  baseURL: '/jxc',
  timeout: 30000
})

// 请求拦截: 自动附加 Authorization 头
service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截: 业务码 200 解包返回 data; 401 跳登录; 其他弹错误
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res && res.code === 200) {
      return res.data
    }
    // 业务错误(改为 warning 橙色,业务限制≠系统错误)
    Message({ message: (res && res.msg) || '系统异常', type: 'warning', duration: 3000 })
    return Promise.reject(new Error((res && res.msg) || 'Error'))
  },
  error => {
    const status = error.response && error.response.status
    if (status === 401) {
      MessageBox.confirm('登录状态已失效,请重新登录', '提示', {
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        removeToken()
        removeUserInfo()
        window.location.href = '/#/login'
      }).catch(() => {})
    } else {
      const msg = (error.response && error.response.data && error.response.data.msg) || error.message || '请求异常'
      Message({ message: msg, type: 'error', duration: 3000 })
    }
    return Promise.reject(error)
  }
)

export default service
