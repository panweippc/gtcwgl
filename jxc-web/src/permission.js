// 路由守卫: 未登录跳转 /login, 已登录按菜单权限控制访问
import router from './router'
import store from './store'
import { getToken } from './utils/auth'

const whiteList = ['/login']

router.beforeEach((to, from, next) => {
  const hasToken = getToken()
  if (hasToken) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      // 菜单访问控制: 非超管只能访问已分配菜单页面(首页始终可访问)
      const roleCode = store.getters['user/roleCode']
      const isHome = to.path === '/' || to.path === '/dashboard'
      if (roleCode && roleCode !== 'ROLE_ADMIN' && !isHome && to.meta && to.meta.title) {
        const menus = store.getters['user/menus'] || []
        const assigned = new Set()
        menus.forEach(m => {
          assigned.add(m.menuName)
          ;(m.children || []).forEach(c => assigned.add(c.menuName))
        })
        if (!assigned.has(to.meta.title)) {
          next('/dashboard')
          return
        }
      }
      next()
    }
  } else {
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})
