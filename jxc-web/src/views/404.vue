<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<script>
export default { name: 'NotFound' }
</script>

<style scoped>
.not-found { text-align: center; padding: 80px; }
.not-found h1 { font-size: 80px; color: #409EFF; margin: 0; }
.not-found p { color: #909399; margin: 12px 0 24px; }
</style>
