<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :inline="true" :model="query" class="search-form">
      <el-form-item label="登录账号">
        <el-input v-model="query.username" placeholder="请输入登录账号" clearable @keyup.enter.native="handleSearch" />
      </el-form-item>
      <el-form-item label="用户姓名">
        <el-input v-model="query.realName" placeholder="请输入用户姓名" clearable @keyup.enter.native="handleSearch" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        <el-button type="success" icon="el-icon-plus" @click="handleAdd">新增用户</el-button>
      </el-form-item>
    </el-form>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column label="登录账号" prop="username" min-width="120" />
      <el-table-column label="用户姓名" prop="realName" min-width="120" />
      <el-table-column label="手机号" prop="phone" min-width="130" />
      <el-table-column label="邮箱" prop="email" min-width="180" show-overflow-tooltip />
      <el-table-column label="角色" prop="roleName" min-width="120" />
      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="{ row }">
          <el-switch
            :value="row.status === 1"
            active-color="#13ce66"
            inactive-color="#ff4949"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="最后登录时间" prop="lastLoginTime" min-width="170" />
      <el-table-column label="操作" width="240" fixed="right">
        <template slot-scope="{ row }">
          <el-button type="text" icon="el-icon-edit" @click="handleEdit(row)">编辑</el-button>
          <el-button type="text" icon="el-icon-key" @click="handleResetPwd(row)">重置密码</el-button>
          <el-button type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      background
      :current-page="query.page"
      :page-size="query.size"
      :page-sizes="[10, 20, 50]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top:16px;text-align:right"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="540px" @close="resetForm">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-divider content-position="left">基础信息</el-divider>
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
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" :readonly="isEdit" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="登录密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入登录密码" />
        </el-form-item>
        <el-form-item label="用户姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入用户姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width:100%">
            <el-option
              v-for="r in roleOptions"
              :key="r.id"
              :label="r.roleName"
              :value="r.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-divider content-position="left">详细信息</el-divider>
        <el-form-item label="用户性别">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog title="重置密码" :visible.sync="pwdVisible" width="420px">
      <el-form ref="pwdForm" :model="pwdForm" :rules="pwdRules" label-width="100px">
        <el-form-item label="用户名">
          <span>{{ pwdForm.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="pwdForm.password" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="pwdVisible = false">取 消</el-button>
        <el-button type="primary" :loading="pwdSubmitting" @click="handlePwdSubmit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getUserPage, addUser, updateUser, deleteUser,
  resetPassword, toggleUserStatus, getAllRoles
} from '@/api/system'
import { getToken } from '@/utils/auth'

export default {
  name: 'SystemUser',
  data() {
    return {
      loading: false,
      submitting: false,
      pwdSubmitting: false,
      list: [],
      total: 0,
      query: { page: 1, size: 10, username: '', realName: '', status: undefined },
      roleOptions: [],
      dialogVisible: false,
      isEdit: false,
      form: this.buildForm(),
      rules: {
        username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
        password: [
          { required: true, message: '请输入登录密码', trigger: 'blur' },
          { min: 6, message: '密码长度不少于 6 位', trigger: 'blur' }
        ],
        realName: [{ required: true, message: '请输入用户姓名', trigger: 'blur' }],
        roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }]
      },
      pwdVisible: false,
      pwdForm: { userId: undefined, username: '', password: '' },
      pwdRules: {
        password: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不少于 6 位', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    dialogTitle() { return this.isEdit ? '编辑用户' : '新增用户' },
    uploadAction() { return '/jxc/system/file/upload' },
    uploadHeaders() { return { Authorization: 'Bearer ' + getToken() } }
  },
  created() {
    this.fetchList()
    this.fetchRoles()
  },
  methods: {
    buildForm() {
      return {
        id: undefined,
        username: '',
        password: '',
        avatar: '',
        realName: '',
        gender: 1,
        phone: '',
        email: '',
        roleId: undefined,
        status: 1,
        remark: ''
      }
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
    async fetchList() {
      this.loading = true
      try {
        const data = await getUserPage(this.query)
        this.list = (data && data.records) || []
        this.total = (data && data.total) || 0
      } catch (e) { /* 忽略 */ } finally {
        this.loading = false
      }
    },
    async fetchRoles() {
      try {
        this.roleOptions = await getAllRoles() || []
      } catch (e) { /* 忽略 */ }
    },
    handleSearch() { this.query.page = 1; this.fetchList() },
    handleReset() {
      this.query = { page: 1, size: 10, username: '', realName: '', status: undefined }
      this.fetchList()
    },
    handlePageChange(p) { this.query.page = p; this.fetchList() },
    handleSizeChange(s) { this.query.size = s; this.query.page = 1; this.fetchList() },
    handleAdd() {
      this.isEdit = false
      this.form = this.buildForm()
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEdit = true
      this.form = Object.assign(this.buildForm(), row)
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        this.submitting = true
        try {
          if (this.isEdit) {
            await updateUser(this.form)
            this.$message.success('修改成功')
          } else {
            const { password, ...userData } = this.form
            await addUser(userData, password)
            this.$message.success('新增成功')
          }
          this.dialogVisible = false
          this.fetchList()
        } catch (e) { /* 忽略 */ } finally {
          this.submitting = false
        }
      })
    },
    handleDelete(row) {
      this.$confirm(`确认删除用户 [${row.username}] 吗?`, '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteUser(row.id)
          this.$message.success('删除成功')
          this.fetchList()
        } catch (e) { /* 忽略 */ }
      }).catch(() => {})
    },
    handleResetPwd(row) {
      this.pwdForm = { userId: row.id, username: row.username, password: '' }
      this.pwdVisible = true
      this.$nextTick(() => { this.$refs.pwdForm && this.$refs.pwdForm.clearValidate() })
    },
    handlePwdSubmit() {
      this.$refs.pwdForm.validate(async valid => {
        if (!valid) return
        this.pwdSubmitting = true
        try {
          await resetPassword({ userId: this.pwdForm.userId, password: this.pwdForm.password })
          this.$message.success('密码重置成功')
          this.pwdVisible = false
        } catch (e) { /* 忽略 */ } finally {
          this.pwdSubmitting = false
        }
      })
    },
    handleStatusChange(row) {
      const next = row.status === 1 ? 0 : 1
      const text = next === 1 ? '启用' : '禁用'
      this.$confirm(`确认${text}用户 [${row.username}] 吗?`, '提示', { type: 'warning' })
        .then(async () => {
          try {
            await toggleUserStatus(row.id, next)
            row.status = next
            this.$message.success(`${text}成功`)
          } catch (e) { /* 忽略 */ }
        }).catch(() => {})
    },
    resetForm() {
      this.$refs.form && this.$refs.form.clearValidate()
    }
  }
}
</script>

<style lang="scss" scoped>
.search-form { margin-bottom: 12px; }
/* 覆盖全局推右样式：查询/重置按钮紧跟状态筛选框 */
::v-deep .search-form .el-form-item:last-child { margin-left: 0 !important; }
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
</style>
