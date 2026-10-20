// 多页签 store 模块
const state = {
  // 已访问的页签列表 [{ path, name, meta: { title, icon }, query, fullPath }]
  visitedViews: [],
  // 已缓存的组件名(keep-alive)
  cachedViews: []
}

const mutations = {
  // 添加一个页签
  ADD_VISITED_VIEW(state, view) {
    if (state.visitedViews.some(v => v.path === view.path)) return
    state.visitedViews.push({
      name: view.name,
      path: view.path,
      query: view.query,
      fullPath: view.fullPath,
      meta: view.meta || {}
    })
  },
  // 添加到缓存
  ADD_CACHED_VIEW(state, view) {
    if (state.cachedViews.includes(view.name)) return
    if (view.meta && view.meta.noCache) return
    if (view.name) state.cachedViews.push(view.name)
  },
  // 关闭一个页签
  DEL_VISITED_VIEW(state, view) {
    return new Promise(resolve => {
      const i = state.visitedViews.findIndex(v => v.path === view.path)
      if (i > -1) {
        state.visitedViews.splice(i, 1)
      }
      resolve([...state.visitedViews])
    })
  },
  // 从缓存移除
  DEL_CACHED_VIEW(state, view) {
    const idx = state.cachedViews.indexOf(view.name)
    if (idx > -1) state.cachedViews.splice(idx, 1)
  },
  // 关闭其他
  DEL_OTHERS_VISITED_VIEWS(state, view) {
    return new Promise(resolve => {
      state.visitedViews = state.visitedViews.filter(v => {
        return v.meta && v.meta.affix || v.path === view.path
      })
      state.cachedViews = state.cachedViews.filter(name => name === view.name)
      resolve([...state.visitedViews])
    })
  },
  // 关闭全部
  DEL_ALL_VISITED_VIEWS(state) {
    return new Promise(resolve => {
      state.visitedViews = state.visitedViews.filter(v => v.meta && v.meta.affix)
      state.cachedViews = []
      resolve([...state.visitedViews])
    })
  }
}

const actions = {
  addView({ commit }, view) {
    commit('ADD_VISITED_VIEW', view)
    commit('ADD_CACHED_VIEW', view)
  },
  delView({ commit, state }, view) {
    return new Promise(resolve => {
      commit('DEL_VISITED_VIEW', view)
      commit('DEL_CACHED_VIEW', view)
      resolve([...state.visitedViews])
    })
  },
  delOthersViews({ commit, state }, view) {
    return new Promise(resolve => {
      commit('DEL_OTHERS_VISITED_VIEWS', view)
      commit('DEL_CACHED_VIEW', view)
      resolve([...state.visitedViews])
    })
  },
  delAllViews({ commit, state }) {
    return new Promise(resolve => {
      commit('DEL_ALL_VISITED_VIEWS')
      commit('DEL_CACHED_VIEW', { name: state.cachedViews[0] })
      resolve([...state.visitedViews])
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
