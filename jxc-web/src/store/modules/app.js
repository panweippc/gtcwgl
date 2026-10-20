// 应用状态: 侧边栏折叠
const state = {
  sidebarOpened: true
}

const mutations = {
  TOGGLE_SIDEBAR: (state) => {
    state.sidebarOpened = !state.sidebarOpened
  }
}

const actions = {
  toggleSidebar({ commit }) {
    commit('TOGGLE_SIDEBAR')
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
