<template>
  <div class="navbar">
    <i
      class="toggle-icon"
      :class="$store.state.app.sidebarOpened ? 'el-icon-s-fold' : 'el-icon-s-unfold'"
      @click="toggleSidebar"
    ></i>
    <tags-view class="navbar-tags" />
    <div class="right">
      <el-dropdown @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="28" :src="avatar">{{ initials }}</el-avatar>
          <span class="username">{{ displayName }}</span>
          <i class="el-icon-arrow-down"></i>
        </span>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="profile">个人信息</el-dropdown-item>
          <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
    <!-- 个人信息悬浮窗 -->
    <profile-dialog :visible.sync="profileVisible" />
  </div>
</template>

<script>
import TagsView from './TagsView.vue'
import ProfileDialog from './ProfileDialog.vue'

export default {
  name: 'Navbar',
  components: { TagsView, ProfileDialog },
  data() {
    return {
      /** 个人信息悬浮窗显示状态 */
      profileVisible: false
    }
  },
  computed: {
    userInfo() {
      return this.$store.state.user.userInfo || {}
    },
    displayName() {
      return (this.userInfo.realName || this.userInfo.username || '用户')
    },
    avatar() {
      return this.userInfo.avatar || ''
    },
    initials() {
      const n = this.userInfo.realName || this.userInfo.username || '?'
      return n.charAt(0).toUpperCase()
    }
  },
  methods: {
    toggleSidebar() {
      this.$store.dispatch('app/toggleSidebar')
    },
    handleCommand(cmd) {
      if (cmd === 'logout') {
        this.$confirm('确认退出登录?', '提示', { type: 'warning' })
          .then(() => {
            this.$store.dispatch('user/logout').then(() => {
              this.$router.push('/login')
            })
          })
          .catch(() => {})
      } else if (cmd === 'profile') {
        // 打开个人信息悬浮窗
        this.profileVisible = true
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  display: flex;
  align-items: center;
  height: 50px;
  width: 100%;
}
.toggle-icon {
  font-size: 20px;
  cursor: pointer;
  margin-right: 12px;
  flex-shrink: 0;
}
.navbar-tags {
  flex: 1;
  min-width: 0;
  height: 100%;
}
.right {
  flex-shrink: 0;
  margin-left: auto;
}
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  outline: none;
}
.username {
  margin: 0 6px;
  color: #303133;
}
</style>
