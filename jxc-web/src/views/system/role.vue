<template>
  <div class="app-container">
    <!-- 列表工具栏 -->
    <div class="list-toolbar">
      <el-button type="success" icon="el-icon-plus" @click="handleAdd">新增角色</el-button>
    </div>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column label="角色名称" prop="roleName" min-width="140" />
      <el-table-column label="角色编码" prop="roleCode" min-width="140" />
      <el-table-column label="数据范围" width="120" align="center">
        <template slot-scope="{ row }">
          <el-tag size="small" :type="row.dataScope === 1 ? 'success' : 'info'">
            {{ dataScopeText(row.dataScope) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="sort" width="80" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="240" fixed="right">
        <template slot-scope="{ row }">
          <el-button type="text" icon="el-icon-edit" @click="handleEdit(row)">编辑</el-button>
          <el-button type="text" icon="el-icon-share" @click="handleAssign(row)">分配菜单</el-button>
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
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px" @close="resetForm">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="数据范围" prop="dataScope">
          <el-select v-model="form.dataScope" placeholder="请选择数据范围" style="width:100%">
            <el-option label="全部数据" :value="1" />
            <el-option label="本人数据" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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

    <!-- 分配菜单弹窗 -->
    <el-dialog title="分配菜单" :visible.sync="assignVisible" width="480px">
      <div v-loading="assignLoading">
        <el-tree
          v-if="!assignLoading"
          ref="menuTree"
          :data="menuTree"
          show-checkbox
          node-key="id"
          :props="{ label: 'menuName', children: 'children' }"
          :default-checked-keys="checkedKeys"
          :default-expand-all="true"
        />
      </div>
      <div slot="footer">
        <el-button @click="assignVisible = false">取 消</el-button>
        <el-button type="primary" :loading="assignSubmitting" @click="handleAssignSubmit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getRolePage, addRole, updateRole, deleteRole,
  assignMenus, getRoleMenus, getMenuTree
} from '@/api/system'

export default {
  name: 'SystemRole',
  data() {
    return {
      loading: false,
      submitting: false,
      assignLoading: false,
      assignSubmitting: false,
      list: [],
      total: 0,
      query: { page: 1, size: 10 },
      dialogVisible: false,
      isEdit: false,
      form: this.buildForm(),
      rules: {
        roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
        roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
        dataScope: [{ required: true, message: '请选择数据范围', trigger: 'change' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }]
      },
      assignVisible: false,
      menuTree: [],
      checkedKeys: [],
      currentRoleId: undefined
    }
  },
  computed: {
    dialogTitle() { return this.isEdit ? '编辑角色' : '新增角色' }
  },
  created() { this.fetchList() },
  methods: {
    buildForm() {
      return {
        id: undefined,
        roleName: '',
        roleCode: '',
        dataScope: 1,
        sort: 0,
        status: 1,
        remark: ''
      }
    },
    dataScopeText(scope) {
      if (scope === 1) return '全部数据'
      if (scope === 2) return '本人数据'
      return '-'
    },
    async fetchList() {
      this.loading = true
      try {
        const data = await getRolePage(this.query)
        this.list = (data && data.records) || []
        this.total = (data && data.total) || 0
      } catch (e) { /* 忽略 */ } finally {
        this.loading = false
      }
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
            await updateRole(this.form)
            this.$message.success('修改成功')
          } else {
            await addRole(this.form)
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
      this.$confirm(`确认删除角色 [${row.roleName}] 吗?`, '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deleteRole(row.id)
            this.$message.success('删除成功')
            this.fetchList()
          } catch (e) { /* 忽略 */ }
        }).catch(() => {})
    },
    async handleAssign(row) {
      this.currentRoleId = row.id
      this.checkedKeys = []
      this.assignVisible = true
      this.assignLoading = true
      try {
        // 并行加载菜单树与已选 ID
        const [tree, checked] = await Promise.all([getMenuTree(), getRoleMenus(row.id)])
        this.menuTree = tree || []
        // 只默认勾选叶子菜单, 父目录由 el-tree 级联自动置为半选;
        // 若把目录 ID 也放入默认勾选, el-tree 会级联勾选其全部子菜单, 导致回显虚增
        const assigned = new Set((checked || []).map(id => Number(id)))
        const leafIds = []
        const collectLeafIds = nodes => (nodes || []).forEach(n => {
          if (n.children && n.children.length) {
            collectLeafIds(n.children)
          } else if (assigned.has(Number(n.id))) {
            leafIds.push(n.id)
          }
        })
        collectLeafIds(this.menuTree)
        this.checkedKeys = leafIds
      } catch (e) { /* 忽略 */ } finally {
        this.assignLoading = false
      }
    },
    async handleAssignSubmit() {
      if (!this.currentRoleId) return
      const tree = this.$refs.menuTree
      const checked = tree.getCheckedKeys()
      const half = tree.getHalfCheckedKeys()
      const menuIds = [...checked, ...half].map(id => Number(id))
      this.assignSubmitting = true
      try {
        await assignMenus({ roleId: this.currentRoleId, menuIds })
        this.$message.success('分配菜单成功')
        this.assignVisible = false
      } catch (e) { /* 忽略 */ } finally {
        this.assignSubmitting = false
      }
    },
    resetForm() { this.$refs.form && this.$refs.form.clearValidate() }
  }
}
</script>

<style lang="scss" scoped>
.list-toolbar { margin-bottom: 12px; text-align: right; }
</style>
