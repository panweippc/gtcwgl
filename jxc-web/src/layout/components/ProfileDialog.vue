<template>
  <el-dialog
    title="个人中心"
    :visible="visible"
    width="520px"
    custom-class="profile-dialog"
    @update:visible="$emit('update:visible', $event)"
    @open="loadData"
  >
    <div class="profile-body">
      <!-- 基础账号信息(只读) -->
      <el-form :model="form" label-width="100px" class="profile-form">
        <el-divider content-position="left">基础账号信息</el-divider>
        <el-form-item label="登录账号">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="当前角色">
          <el-input :value="roleName" disabled />
        </el-form-item>
        <el-form-item label="账号状态">
          <el-input :value="form.status === 1 ? '启用' : '禁用'" disabled />
        </el-form-item>
        <el-form-item label="首次创建">
          <el-input :value="form.createTime" disabled />
        </el-form-item>
        <el-form-item label="最近登录">
          <el-input :value="form.lastLoginTime" disabled />
        </el-form-item>
      </el-form>

      <!-- 可编辑信息 -->
      <el-form ref="profileForm" :model="form" :rules="rules" label-width="100px" class="profile-form">
        <el-divider content-position="left">可编辑信息</el-divider>
        <el-form-item label="用户头像">
          <el-upload
            class="avatar-uploader"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            accept="image/*"
          >
            <img v-if="form.avatar" :src="form.avatar" class="avatar-img" />
            <i v-else class="el-icon-plus avatar-uploader-icon" />
          </el-upload>
        </el-form-item>
        <el-form-item label="用户姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入用户姓名" maxlength="32" />
        </el-form-item>
        <el-form-item label="用户性别">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="64" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存修改</el-button>
        </el-form-item>
      </el-form>

      <!-- 修改登录密码 -->
      <el-form ref="pwdForm" :model="pwdForm" :rules="pwdRules" label-width="100px" class="profile-form">
        <el-divider content-position="left">修改登录密码</el-divider>
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码(至少6位)" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="warning" :loading="changing" @click="changePwd">修改密码</el-button>
        </el-form-item>
      </el-form>
    </div>
  </el-dialog>
</template>

<script>
import { getProfile, updateProfile, changePassword, getAllRoles } from '@/api/system'
import { getToken } from '@/utils/auth'

export default {
  name: 'ProfileDialog',
  props: {
    /** 弹窗显示状态 */
    visible: { type: Boolean, default: false }
  },
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.pwdForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      form: {
        id: null, username: '', realName: '', gender: 1, phone: '', email: '',
        avatar: '', remark: '', roleId: undefined, status: 1,
        createTime: '', lastLoginTime: ''
      },
      roleOptions: [],
      rules: {
        realName: [{ required: true, message: '请输入用户姓名', trigger: 'blur' }],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ]
      },
      saving: false,
      pwdForm: { oldPassword: '', newPassword: '', confirmPassword: '' },
      pwdRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码至少6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      },
      changing: false
    }
  },
  computed: {
    /** 根据角色ID匹配角色名称 */
    roleName() {
      const role = this.roleOptions.find(r => r.id === this.form.roleId)
      return role ? role.roleName : ''
    },
    uploadAction() { return '/jxc/system/file/upload' },
    uploadHeaders() { return { Authorization: 'Bearer ' + getToken() } }
  },
  methods: {
    /** 弹窗打开时加载个人信息与角色列表 */
    loadData() {
      this.loadProfile()
      this.loadRoles()
    },
    /** 头像上传成功回调 */
    handleAvatarSuccess(res) {
      if (res && res.code === 200) {
        this.form.avatar = res.data
      } else {
        this.$message.error(res.msg || '头像上传失败')
      }
    },
    /** 头像上传前校验 */
    beforeAvatarUpload(file) {
      const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/bmp'].includes(file.type)
      if (!isImage) {
        this.$message.error('仅支持 jpg/png/gif/bmp 格式图片')
        return false
      }
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isLt2M) {
        this.$message.error('头像图片大小不能超过 2MB')
        return false
      }
      return true
    },
    loadProfile() {
      getProfile().then(data => {
        Object.assign(this.form, data)
      })
    },
    /** 加载角色列表用于匹配角色名称 */
    loadRoles() {
      getAllRoles().then(data => {
        this.roleOptions = data || []
      }).catch(() => {})
    },
    save() {
      this.$refs.profileForm.validate(valid => {
        if (!valid) return
        this.saving = true
        const { id, username, createTime, lastLoginTime, status, roleId, ...data } = this.form
        updateProfile(data).then(() => {
          this.$message.success('个人信息已更新')
          // 更新 store 中的 userInfo(导航栏姓名头像实时刷新)
          this.$store.dispatch('user/getUserInfo').catch(() => {})
        }).finally(() => {
          this.saving = false
        })
      })
    },
    changePwd() {
      this.$refs.pwdForm.validate(valid => {
        if (!valid) return
        this.changing = true
        changePassword({
          oldPassword: this.pwdForm.oldPassword,
          newPassword: this.pwdForm.newPassword
        }).then(() => {
          this.$message.success('密码修改成功,请重新登录')
          this.pwdForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
          this.$emit('update:visible', false)
          // 延迟退出让用户看到提示
          setTimeout(() => {
            this.$store.dispatch('user/logout').then(() => {
              this.$router.push('/login')
            })
          }, 1500)
        }).finally(() => {
          this.changing = false
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .profile-dialog {
  margin-top: 6vh !important;
  .el-dialog__body {
    max-height: 76vh;
    overflow-y: auto;
    padding-top: 10px;
  }
}
.profile-form { padding: 0 8px; }
.avatar-uploader {
  ::v-deep .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    overflow: hidden;
    width: 100px;
    height: 100px;
    display: flex;
    align-items: center;
    justify-content: center;
    &:hover { border-color: #409EFF; }
  }
}
.avatar-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
::v-deep .el-divider--horizontal {
  margin: 8px 0 18px;
  .el-divider__text {
    font-weight: 600;
    color: #303133;
  }
}
</style>
