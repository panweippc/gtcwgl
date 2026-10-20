<template>
  <!--
    关键修复: 用 component 动态选择根元素类型, 避免 Vue2 单根限制产生的多余 <div> 包裹层
    SidebarItem 渲染出来直接是 <el-menu-item> 或 <el-submenu>, 中间没有任何 <div>
    这样 ElementUI collapse popup 才能正确计算尺寸
  -->
  <component :is="rootTag" :index="rootIndex">
    <!-- el-menu-item 分支: 单叶子折叠 / 纯叶子节点 -->
    <template v-if="!isSubmenu">
      <i v-if="itemIcon" :class="itemIcon"></i>
      <span slot="title">{{ itemTitle }}</span>
    </template>
    <!-- el-submenu 分支: 多子节点 -->
    <template v-else>
      <template slot="title">
        <i v-if="depth === 0 && icon" :class="icon"></i>
        <span>{{ title }}</span>
      </template>
      <sidebar-item
        v-for="child in visibleChildren"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(child.path)"
        :depth="depth + 1"
      />
    </template>
  </component>
</template>

<script>
export default {
  name: 'SidebarItem',
  props: {
    item: { type: Object, required: true },
    basePath: { type: String, default: '' },
    depth: { type: Number, default: 0 }
  },
  computed: {
    /** 可见子节点列表 */
    visibleChildren() {
      const list = this.item.children || []
      return list.filter(c => !c.hidden)
    },
    /** 路由标题 */
    title() {
      return (this.item.meta && this.item.meta.title) || ''
    },
    /** 路由图标 */
    icon() {
      return (this.item.meta && this.item.meta.icon) || ''
    },
    /** 是否 alwaysShow */
    alwaysShow() {
      return this.item.meta && this.item.meta.alwaysShow
    },
    /**
     * 是否渲染为 submenu
     * 条件: 有多个可见子节点, 或虽然只有 1 个但该子节点本身也有子节点 / 路由标记了 alwaysShow
     */
    isSubmenu() {
      const vc = this.visibleChildren
      if (vc.length === 0) return false
      if (vc.length === 1 && !vc[0].children && !this.alwaysShow) return false
      return true
    },
    /** 根元素 tag: el-submenu 或 el-menu-item */
    rootTag() {
      return this.isSubmenu ? 'el-submenu' : 'el-menu-item'
    },
    /** 根元素 index 属性 */
    rootIndex() {
      if (this.isSubmenu) return this.basePath
      // 单叶子折叠: index 是子节点完整路径
      if (this.visibleChildren.length === 1 && !this.visibleChildren[0].children && !this.alwaysShow) {
        return this.resolvePath(this.visibleChildren[0].path)
      }
      // 纯叶子: index 就是 basePath
      return this.basePath
    },
    /** el-menu-item 的图标 class */
    itemIcon() {
      // 单叶子折叠: 图标取子节点的 (depth===0 时)
      if (this.visibleChildren.length === 1 && !this.visibleChildren[0].children && !this.alwaysShow) {
        if (this.depth === 0) {
          return this.icon || this.iconOf(this.visibleChildren[0])
        }
        return this.iconOf(this.visibleChildren[0])
      }
      // 纯叶子: 图标取自己的 meta.icon
      return this.icon
    },
    /** el-menu-item 的标题文字 */
    itemTitle() {
      if (this.visibleChildren.length === 1 && !this.visibleChildren[0].children && !this.alwaysShow) {
        return this.titleOf(this.visibleChildren[0])
      }
      return this.title
    }
  },
  methods: {
    /** 拼接完整路由路径 */
    resolvePath(routePath) {
      if (/^https?:\/\//.test(routePath)) return routePath
      const base = this.basePath.endsWith('/') ? this.basePath.slice(0, -1) : this.basePath
      const child = routePath.startsWith('/') ? routePath : '/' + routePath
      return base + child
    },
    /** 取子节点标题 */
    titleOf(child) {
      return (child.meta && child.meta.title) || child.name || ''
    },
    /** 取子节点图标 */
    iconOf(child) {
      return (child.meta && child.meta.icon) || ''
    }
  }
}
</script>
