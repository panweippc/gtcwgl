<template>
  <el-container class="app-wrapper">
    <el-aside :width="$store.state.app.sidebarOpened ? '210px' : '64px'" class="sidebar-container">
      <sidebar />
    </el-aside>
    <el-container class="main-container">
      <el-header class="navbar" height="50px">
        <navbar />
      </el-header>
      <el-main class="app-main">
        <keep-alive :include="cachedViews">
          <router-view />
        </keep-alive>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'

export default {
  name: 'Layout',
  components: { Sidebar, Navbar },
  computed: {
    cachedViews() {
      return this.$store.state.tagsView.cachedViews
    }
  }
}
</script>

<style lang="scss" scoped>
/* 根容器: 占满视口, 禁止任何外层滚动 */
.app-wrapper {
  height: 100vh !important;
  width: 100vw !important;
  overflow: hidden !important;
  margin: 0 !important;
  padding: 0 !important;
}
/* 侧边栏容器 */
.sidebar-container {
  background: #304156;
  transition: width 0.28s;
  overflow: visible;
}
/* 右侧主容器 */
.main-container {
  height: 100% !important;
  overflow: hidden !important;
  display: flex !important;
  flex-direction: column !important;
}
.navbar {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 16px !important;
  flex-shrink: 0;
  display: flex !important;
  align-items: center !important;
}
/* ★ 唯一允许滚动的地方 */
.app-main {
  background: #f0f2f5;
  padding: 16px !important;
  overflow: auto !important;
  flex: 1 !important;
  min-height: 0 !important;
}
</style>
