<template>
  <div class="app-container">
    <div class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" icon="el-icon-plus" @click="handleAddRoot">新增分类</el-button>
      </div>
      <el-table
        v-loading="tableLoading"
        :data="tableData"
        row-key="id"
        :tree-props="{ children: 'children' }"
        :key="tableKey"
        border
        stripe
        default-expand-all
      >
        <el-table-column prop="categoryName" label="分类名称" min-width="180" />
        <el-table-column prop="categoryCode" label="分类编码" min-width="140" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="320" align="center" fixed="right" class-name="col-action">
          <template slot-scope="{ row }">
            <el-button type="text" icon="el-icon-view" @click="handleView(row)">查看</el-button>
            <el-button type="text" icon="el-icon-plus" @click="handleAddChild(row)">新增子分类</el-button>
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" style="color:#f56c6c" icon="el-icon-delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :visible.sync="dialogVisible" :title="dialogTitle" width="520px" @close="handleDialogClose">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" maxlength="50" :disabled="isView" />
        </el-form-item>
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="form.categoryCode" placeholder="请输入分类编码" maxlength="50" :disabled="isView" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" :disabled="isView" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" :disabled="isView" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息" maxlength="200" :disabled="isView" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">{{ isView ? '关 闭' : '取 消' }}</el-button>
        <el-button v-if="!isView" type="primary" @click="handleSubmit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getProductCategoryTreeAll,
  addProductCategory,
  updateProductCategory,
  deleteProductCategory
} from '@/api/product'

export default {
  name: 'ProductCategory',
  data() {
    return {
      tableData: [],
      tableKey: 0,
      tableLoading: false,
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      isView: false,
      form: this.buildEmptyForm(),
      rules: {
        categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
        categoryCode: [{ required: true, message: '请输入分类编码', trigger: 'blur' }],
        sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    /** 构建空表单对象 */
    buildEmptyForm() {
      return {
        id: null,
        parentId: 0,
        categoryName: '',
        categoryCode: '',
        sort: 0,
        status: 1,
        remark: ''
      }
    },
    /** 加载分类树 */
    async loadData() {
      this.tableLoading = true
      try {
        const data = await getProductCategoryTreeAll()
        this.tableKey++
        this.tableData = Array.isArray(data) ? data : []
      } catch (e) {
        // 错误已由拦截器弹出
      } finally {
        this.tableLoading = false
      }
    },
    /** 新增分类(根) */
    handleAddRoot() {
      this.openDialog('add', null)
    },
    /** 新增子分类 */
    handleAddChild(row) {
      this.openDialog('add', row)
    },
    /** 编辑 */
    handleEdit(row) {
      this.openDialog('edit', row)
    },
    /** 查看 */
    handleView(row) {
      this.openDialog('view', row)
    },
    /**
     * 打开弹窗
     * @param action 'add' | 'edit' | 'view'
     * @param row 编辑/查看时为当前行,新增子分类时为父行
     */
    openDialog(action, row) {
      this.isEdit = action === 'edit'
      this.isView = action === 'view'
      const titleMap = { add: '新增分类', edit: '编辑分类', view: '查看分类' }
      this.dialogTitle = titleMap[action]

      if (action === 'edit' || action === 'view') {
        // 编辑/查看: 保留原父级,不做修改
        this.form = { ...this.buildEmptyForm(), ...row }
      } else {
        this.form = this.buildEmptyForm()
        if (row) {
          // 新增子分类,父级为当前行
          this.form.parentId = row.id
        }
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.clearValidate()
      })
    },
    /** 提交表单 */
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        const payload = { ...this.form }
        try {
          if (this.isEdit) {
            await updateProductCategory(payload)
          } else {
            await addProductCategory(payload)
          }
          this.$message.success('操作成功')
          this.dialogVisible = false
          this.loadData()
        } catch (e) {
          // 错误已由拦截器弹出
        }
      })
    },
    /** 删除 */
    handleDelete(row) {
      this.$confirm('确认删除该条记录?', '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deleteProductCategory(row.id)
            this.$message.success('操作成功')
            this.loadData()
          } catch (e) {
            // 错误已由拦截器弹出
          }
        })
        .catch(() => {})
    },
    /** 关闭弹窗时重置表单 */
    handleDialogClose() {
      this.form = this.buildEmptyForm()
      this.$refs.form && this.$refs.form.clearValidate()
    }
  }
}
</script>

<style lang="scss" scoped>
.table-toolbar {
  margin-bottom: 12px;
}
.dialog-footer {
  text-align: right;
}
/* 操作列强制不换行 */
::v-deep .col-action,
::v-deep .col-action .cell {
  white-space: nowrap !important;
}
</style>
