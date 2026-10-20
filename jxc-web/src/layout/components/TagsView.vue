<template>
  <div class="tags-view-wrap">
    <i v-if="overflowed" class="nav-btn el-icon-arrow-left" :class="{ disabled: !canScrollLeft }" @click="onScrollLeft"></i>
    <div ref="scroller" class="tags-view-scroller" @scroll="onScrollerScroll">
      <router-link
        v-for="tag in visitedViews"
        :key="tag.path"
        :to="{ path: tag.path, query: tag.query }"
        class="tags-view-item"
        :class="{ active: isActive(tag) }"
      >
        <span class="tag-title">{{ tag.meta && tag.meta.title ? tag.meta.title : tag.path }}</span>
        <i
          v-if="!isAffix(tag)"
          class="el-icon-close tag-close"
          @click.prevent.stop="closeTag(tag)"
        ></i>
      </router-link>
    </div>
    <i v-if="overflowed" class="nav-btn el-icon-arrow-right" :class="{ disabled: !canScrollRight }" @click="onScrollRight"></i>
  </div>
</template>

<script>
export default {
  name: 'TagsView',
  data() {
    return {
      overflowed: false,
      canScrollLeft: false,
      canScrollRight: false
    }
  },
  computed: {
    visitedViews() {
      return this.$store.state.tagsView.visitedViews
    }
  },
  watch: {
    $route() {
      this.addTags()
      this.$nextTick(() => {
        this.checkOverflow()
        this.scrollToActive()
      })
    },
    visitedViews: {
      immediate: true,
      handler(newList, oldList) {
        // 先跳过 addTags, 只处理 DOM 更新后的滚动状态
        // 因为 addTags 已经在 $route watcher 和 mounted 里调用过了
        if (oldList && newList.length < oldList.length) {
          // 关闭 tag: 确保 DOM 移除后再同步滚动
          this.$nextTick(() => {
            this.$nextTick(() => {
              this.checkOverflow()
              this.fixScrollLeft()
            })
          })
        } else {
          // 添加 tag: 正常
          this.$nextTick(() => this.checkOverflow())
        }
      }
    }
  },
  mounted() {
    this.addTags()
    this.$nextTick(() => {
      this.checkOverflow()
      this.scrollToActive()
    })
    window.addEventListener('resize', this.onResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.onResize)
  },
  methods: {
    isActive(tag) {
      return tag.path === this.$route.path
    },
    isAffix(tag) {
      return tag.meta && tag.meta.affix
    },
    addTags() {
      const { name, meta, path, query, fullPath } = this.$route
      if (!name) return
      this.$store.dispatch('tagsView/addView', { name, meta, path, query, fullPath })
    },
    closeTag(view) {
      const isActive = this.isActive(view)
      this.$store.dispatch('tagsView/delView', view).then(visitedViews => {
        if (isActive) {
          const latestView = visitedViews[visitedViews.length - 1]
          if (latestView) {
            this.$router.push(latestView.fullPath || latestView.path)
          } else {
            this.$router.push('/dashboard')
          }
        }
        // 用双层 nextTick 确保 DOM 彻底移除后再测量 scrollWidth
        this.$nextTick(() => {
          this.$nextTick(() => {
            this.checkOverflow()
            this.fixScrollLeft()
          })
        })
      })
    },
    onScrollerScroll() {
      this.updateScrollState()
    },
    onScrollLeft() {
      const el = this.$refs.scroller
      if (!el) return
      el.scrollBy({ left: -150, behavior: 'smooth' })
    },
    onScrollRight() {
      const el = this.$refs.scroller
      if (!el) return
      el.scrollBy({ left: 150, behavior: 'smooth' })
    },
    checkOverflow() {
      const el = this.$refs.scroller
      if (!el) return
      this.overflowed = el.scrollWidth > el.clientWidth + 1
      this.updateScrollState()
    },
    /**
     * 修正 scrollLeft 越界问题
     * 关闭 tag 后 scrollWidth 变小, 但 scrollLeft 没跟着减, 导致右侧出现空白
     */
    fixScrollLeft() {
      const el = this.$refs.scroller
      if (!el) return
      const maxLeft = el.scrollWidth - el.clientWidth
      if (el.scrollLeft > maxLeft) {
        el.scrollLeft = Math.max(0, maxLeft)
      }
    },
    updateScrollState() {
      const el = this.$refs.scroller
      if (!el) return
      this.canScrollLeft = el.scrollLeft > 2
      this.canScrollRight = el.scrollLeft + el.clientWidth < el.scrollWidth - 2
    },
    scrollToActive() {
      const el = this.$refs.scroller
      if (!el) return
      const active = el.querySelector('.tags-view-item.active')
      if (!active) return
      const containerLeft = el.scrollLeft
      const containerRight = containerLeft + el.clientWidth
      const elLeft = active.offsetLeft
      const elRight = elLeft + active.offsetWidth
      if (elLeft < containerLeft) {
        el.scrollTo({ left: elLeft - 10, behavior: 'smooth' })
      } else if (elRight > containerRight) {
        el.scrollTo({ left: elRight - el.clientWidth + 10, behavior: 'smooth' })
      }
    },
    onResize() {
      this.checkOverflow()
      this.scrollToActive()
    }
  }
}
</script>

<style lang="scss" scoped>
/* 外层: 撑满父级 Navbar 的剩余空间 */
.tags-view-wrap {
  flex: 1;
  min-width: 0;
  height: 100%;
  display: flex;
  align-items: center;
  overflow: hidden;
}
/* 原生滚动容器, 隐藏滚动条 */
.tags-view-scroller {
  flex: 1;
  min-width: 0;
  height: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  white-space: nowrap;
  scrollbar-width: none;
  -ms-overflow-style: none;
  &::-webkit-scrollbar { display: none; width: 0; height: 0; }
  padding-top: 12px;
  padding-bottom: 12px;
  padding-left: 2px;
}
/* 左右手动箭头按钮 */
.nav-btn {
  width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  font-size: 14px;
  border-radius: 3px;
  color: #606266;
  cursor: pointer;
  flex-shrink: 0;
  user-select: none;
  transition: all 0.2s;
  &:hover:not(.disabled) {
    background: #ecf5ff;
    color: #409EFF;
  }
  &:active:not(.disabled) {
    background: #d9ecff;
  }
  &.disabled {
    color: #c0c4cc;
    cursor: not-allowed;
    opacity: 0.5;
  }
}
/* 单个 tag */
.tags-view-item {
  display: inline-flex;
  align-items: center;
  padding: 0 8px 0 10px;
  margin-right: 4px;
  font-size: 12px;
  border: 1px solid #d8dce5;
  border-radius: 2px;
  background: #fff;
  color: #606266;
  text-decoration: none;
  height: 26px;
  line-height: 26px;
  cursor: pointer;
  max-width: 130px;
  vertical-align: top;

  &:hover { color: #409EFF; }

  &.active {
    background: #409EFF;
    border-color: #409EFF;
    color: #fff;
    &:hover { color: #fff; }
    .tag-close:hover {
      background: rgba(255, 255, 255, 0.3);
      color: #fff;
    }
  }
  .tag-title {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .tag-close {
    margin-left: 4px;
    font-size: 12px;
    border-radius: 50%;
    width: 16px;
    height: 16px;
    line-height: 16px;
    text-align: center;
    color: #909399;
    flex-shrink: 0;
    &:hover {
      background: #c0c4cc;
      color: #fff;
    }
  }
}
</style>
