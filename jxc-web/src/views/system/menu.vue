<template>
  <div class="app-container">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <el-button type="success" icon="el-icon-plus" @click="handleAddRoot">新增根菜单</el-button>
      <el-button icon="el-icon-refresh" @click="fetchTree">刷新</el-button>
    </div>

    <!-- 树形列表 -->
    <el-table
      v-loading="loading"
      :data="tree"
      row-key="id"
      border
      stripe
      :tree-props="{ children: 'children' }"
      default-expand-all
    >
      <el-table-column label="菜单名称" prop="menuName" min-width="180" />
      <el-table-column label="图标" width="80" align="center">
        <template slot-scope="{ row }">
          <i v-if="row.icon" :class="row.icon" style="font-size:16px" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="sort" width="80" align="center" />
      <el-table-column label="权限标识" prop="perms" min-width="160" show-overflow-tooltip />
      <el-table-column label="组件路径" prop="component" min-width="180" show-overflow-tooltip />
      <el-table-column label="类型" width="100" align="center">
        <template slot-scope="{ row }">
          <el-tag size="small" :type="typeTag(row.menuType)">{{ typeText(row.menuType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template slot-scope="{ row }">
          <el-button type="text" icon="el-icon-edit" @click="handleEdit(row)">编辑</el-button>
          <el-button type="text" icon="el-icon-plus" @click="handleAddChild(row)">新增子菜单</el-button>
          <el-button type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px" @close="resetForm">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="父级菜单" prop="parentId">
          <el-cascader
            v-model="form.parentId"
            :options="parentOptions"
            :props="cascaderProps"
            :show-all-levels="false"
            placeholder="根菜单选择 0"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio :label="1">目录</el-radio>
            <el-radio :label="2">菜单</el-radio>
            <el-radio :label="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="form.path" placeholder="如: system/user" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="如: system/user/index" />
        </el-form-item>
        <el-form-item label="权限标识" prop="perms">
          <el-input v-model="form.perms" placeholder="如: system:user:list" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="如: el-icon-setting" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="是否显示" prop="visible">
          <el-switch v-model="form.visible" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
  </div>
</template>

<script>
import { getMenuTree, addMenu, updateMenu, deleteMenu } from '@/api/system'

export default {
  name: 'SystemMenu',
  data() {
    return {
      loading: false,
      submitting: false,
      tree: [],
      dialogVisible: false,
      isEdit: false,
      form: this.buildForm(),
      rules: {
        menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
        menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }]
      },
      cascaderProps: {
        value: 'id', label: 'menuName', children: 'children',
        checkStrictly: true, emitPath: false
      }
    }
  },
  computed: {
    dialogTitle() { return this.isEdit ? '编辑菜单' : '新增菜单' },
    parentOptions() {
      const root = [{ id: 0, menuName: '根菜单', children: this.tree }]
      return root
    }
  },
  created() { this.fetchTree() },
  methods: {
    buildForm() {
      return {
        id: undefined,
        parentId: 0,
        menuName: '',
        menuType: 1,
        path: '',
        component: '',
        perms: '',
        icon: '',
        sort: 0,
        visible: 1,
        status: 1,
        remark: ''
      }
    },
    typeText(t) {
      return { 1: '目录', 2: '菜单', 3: '按钮' }[t] || '-'
    },
    typeTag(t) {
      return { 1: '', 2: 'success', 3: 'warning' }[t] || ''
    },
    async fetchTree() {
      this.loading = true
      try {
        this.tree = await getMenuTree() || []
      } catch (e) { /* 忽略 */ } finally {
        this.loading = false
      }
    },
    handleAddRoot() {
      this.isEdit = false
      this.form = this.buildForm()
      this.dialogVisible = true
    },
    handleAddChild(row) {
      this.isEdit = false
      this.form = this.buildForm()
      this.form.parentId = row.id
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
            await updateMenu(this.form)
            this.$message.success('修改成功')
          } else {
            await addMenu(this.form)
            this.$message.success('新增成功')
          }
          this.dialogVisible = false
          this.fetchTree()
        } catch (e) { /* 忽略 */ } finally {
          this.submitting = false
        }
      })
    },
    handleDelete(row) {
      const hasChildren = row.children && row.children.length
      const msg = hasChildren
        ? `菜单 [${row.menuName}] 存在子菜单, 删除将影响其下所有子菜单, 确认删除吗?`
        : `确认删除菜单 [${row.menuName}] 吗?`
      this.$confirm(msg, '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deleteMenu(row.id)
            this.$message.success('删除成功')
            this.fetchTree()
          } catch (e) { /* 忽略 */ }
        }).catch(() => {})
    },
    resetForm() { this.$refs.form && this.$refs.form.clearValidate() }
  }
}
</script>

<style lang="scss" scoped>
.toolbar { margin-bottom: 16px; }
</style>
