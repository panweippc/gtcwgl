<template>
  <div class="sidebar">
    <div class="logo">
      <span v-if="$store.state.app.sidebarOpened">进销存系统</span>
      <span v-else>JXC</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="!$store.state.app.sidebarOpened"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      router
      class="sidebar-menu"
    >
      <sidebar-item
        v-for="route in visibleRoutes"
        v-if="!route.hidden"
        :key="route.path"
        :item="route"
        :base-path="route.path"
      />
    </el-menu>
  </div>
</template>

<script>
import SidebarItem from './SidebarItem.vue'
import { constantRoutes } from '@/router'

export default {
  name: 'Sidebar',
  components: { SidebarItem },
  computed: {
    /** 按用户已分配菜单过滤后的路由(超管显示全部, 保留与管理员一致的嵌套分组布局) */
    visibleRoutes() {
      const roleCode = this.$store.getters['user/roleCode']
      const isAdmin = roleCode === 'ROLE_ADMIN'
      // 收集当前用户已分配的菜单名称(目录+菜单)
      const menus = this.$store.state.user.menus || []
      const assigned = new Set()
      menus.forEach(m => {
        assigned.add(m.menuName)
        ;(m.children || []).forEach(c => assigned.add(c.menuName))
      })
      // 静态路由按菜单名称匹配过滤, 保留 ParentView 嵌套分组层级
      return constantRoutes
        .map(route => {
          // 隐藏路由(登录/404)和首页根路由始终保留
          if (route.hidden) return route
          if (route.path === '/') return route
          // 顶级目录: 管理员或已分配才显示
          const title = route.meta && route.meta.title
          if (!isAdmin && !assigned.has(title)) return null
          // 子路由过滤: 嵌套分组下按孙级菜单过滤, 分组只要有可见子级就保留
          const children = (route.children || []).map(child => {
            if (child.children) {
              const subs = child.children.filter(s => {
                const st = s.meta && s.meta.title
                return isAdmin || !st || assigned.has(st)
              })
              if (!subs.length) return null
              return Object.assign({}, child, { children: subs })
            }
            const ct = child.meta && child.meta.title
            if (!isAdmin && ct && !assigned.has(ct)) return null
            return child
          }).filter(Boolean)
          if (!children.length) return null
          return Object.assign({}, route, { children })
        })
        .filter(Boolean)
    },
    activeMenu() {
      return this.$route.path
    }
  }
}
</script>

<style lang="scss" scoped>
/* 侧边栏占满父容器 */
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.logo {
  height: 50px; line-height: 50px; text-align: center;
  color: #fff; font-size: 18px; font-weight: 600;
  background: #2b3648;
  flex-shrink: 0;
}
/* 菜单占满剩余空间,菜单项过多时纵向滚动 */
.sidebar-menu {
  flex: 1;
  min-height: 0;
  border-right: none;
  background-color: #304156;
  overflow-y: auto;
  overflow-x: hidden;
  /* 隐藏滚动条,保持美观 */
  scrollbar-width: none;
  &::-webkit-scrollbar { display: none; }
}
</style>
