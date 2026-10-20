// 用户模块: token / userInfo / permissions / menus
import { login, getUserInfo, logout } from '@/api/auth'
import {
  getToken,
  setToken,
  removeToken,
  getUserInfo as getStoredUserInfo,
  setUserInfo,
  removeUserInfo
} from '@/utils/auth'

const storedInfo = getStoredUserInfo() || {}

const state = {
  token: getToken(),
  userInfo: storedInfo,
  permissions: storedInfo.permissions || [],
  menus: storedInfo.menus || []
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token || ''
  },
  SET_USER_INFO(state, info) {
    state.userInfo = info || {}
    state.permissions = (info && info.permissions) || []
    state.menus = (info && info.menus) || []
  },
  RESET_STATE(state) {
    state.token = ''
    state.userInfo = {}
    state.permissions = []
    state.menus = []
  }
}

const actions = {
  // 登录: 调 /auth/login, 持久化 token + userInfo
  login({ commit }, payload) {
    return new Promise((resolve, reject) => {
      login(payload).then(data => {
        commit('SET_TOKEN', data.token)
        commit('SET_USER_INFO', data.userInfo)
        setToken(data.token)
        setUserInfo(data.userInfo)
        resolve(data)
      }).catch(reject)
    })
  },
  // 拉取当前用户信息
  getUserInfo({ commit }) {
    return new Promise((resolve, reject) => {
      getUserInfo().then(data => {
        commit('SET_USER_INFO', data)
        setUserInfo(data)
        resolve(data)
      }).catch(reject)
    })
  },
  // 退出
  logout({ commit }) {
    return new Promise((resolve) => {
      logout().finally(() => {
        commit('RESET_STATE')
        removeToken()
        removeUserInfo()
        resolve()
      })
    })
  },
  // 本地清理(无网络调用)
  resetToken({ commit }) {
    commit('RESET_STATE')
    removeToken()
    removeUserInfo()
  }
}

const getters = {
  token: state => state.token,
  userInfo: state => state.userInfo || {},
  permissions: state => state.permissions,
  menus: state => state.menus,
  roleCode: state => (state.userInfo || {}).roleCode,
  realName: state => (state.userInfo || {}).realName,
  roleName: state => (state.userInfo || {}).roleName
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
