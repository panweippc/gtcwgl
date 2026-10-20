<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-title">
        <h2>进销存管理系统</h2>
        <p>JXC Management System</p>
      </div>
      <el-form ref="loginForm" :model="form" :rules="rules" label-width="0" @submit.native.prevent>
        <el-form-item prop="username">
          <el-input v-model="form.username" prefix-icon="el-icon-user" placeholder="用户名" @keyup.enter.native="submit" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" prefix-icon="el-icon-lock" placeholder="密码" show-password @keyup.enter.native="submit" />
        </el-form-item>
        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" prefix-icon="el-icon-key" placeholder="验证码" @keyup.enter.native="submit" maxlength="4" />
            <img class="captcha-img" :src="captchaImg" alt="验证码" title="点击刷新" @click="refreshCaptcha" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width:100%" @click="submit">登 录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { getCaptcha } from '@/api/auth'

export default {
  name: 'Login',
  data() {
    return {
      form: { username: 'admin', password: 'admin123', captchaCode: '', captchaKey: '' },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
      },
      loading: false,
      captchaImg: ''
    }
  },
  created() {
    this.refreshCaptcha()
  },
  methods: {
    /**
     * 刷新验证码
     */
    refreshCaptcha() {
      getCaptcha().then(data => {
        this.captchaImg = data.captchaImg
        this.form.captchaKey = data.captchaKey
        this.form.captchaCode = ''
      }).catch(() => {
        this.$message.error('验证码获取失败,请检查后端服务')
      })
    },
    submit() {
      this.$refs.loginForm.validate(valid => {
        if (!valid) return
        this.loading = true
        this.$store.dispatch('user/login', this.form).then(() => {
          this.$message.success('登录成功')
          const redirect = this.$route.query.redirect || '/'
          this.$router.push(redirect)
        }).catch(() => {
          // 登录失败刷新验证码
          this.refreshCaptcha()
        }).finally(() => {
          this.loading = false
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  display: flex; justify-content: center; align-items: center;
}
.login-box {
  width: 380px; padding: 40px; background: #fff; border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
}
.login-title {
  text-align: center; margin-bottom: 24px;
  h2 { margin: 0 0 4px 0; color: #303133; }
  p { margin: 0; color: #909399; font-size: 12px; }
}
.captcha-row {
  display: flex; gap: 8px; width: 100%;
  .el-input { flex: 1; }
}
.captcha-img {
  height: 40px; cursor: pointer; border-radius: 4px;
  border: 1px solid #dcdfe6; background: #fff;
  &:hover { border-color: #409EFF; }
}
</style>
