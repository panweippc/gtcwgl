// Vue CLI 配置
// 后端 context-path=/jxc, 端口 8080; 代理 /jxc 到后端, 不改写路径
module.exports = {
  transpileDependencies: true,
  lintOnSave: false,
  devServer: {
    port: 9527,
    open: true,
    proxy: {
      '/jxc': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}
