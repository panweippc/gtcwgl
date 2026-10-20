<template>
  <router-view />
</template>

<script>
// ParentView: 仅用于多层级路由的中间占位组件
// 多级路由必须有真实组件,但不需要任何额外 DOM
export default {
  name: 'ParentView'
}
</script>
