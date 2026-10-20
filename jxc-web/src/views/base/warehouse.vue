<template>
  <div class="app-container">
    <!-- 列表(含搜索区) -->
    <div class="table-card">
      <!-- 搜索条件 -->
      <el-form class="search-form" :inline="true" :model="query">
        <el-form-item label="仓库名称">
          <el-input v-model="query.warehouseName" placeholder="仓库名称" clearable @keyup.enter.native="handleSearch" style="width:180px" />
        </el-form-item>
        <el-form-item label="仓库编码">
          <el-input v-model="query.warehouseCode" placeholder="仓库编码" clearable @keyup.enter.native="handleSearch" style="width:180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜 索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重 置</el-button>
        </el-form-item>
      </el-form>

      <!-- 新增按钮工具栏 -->
      <div class="table-toolbar">
        <el-button type="success" icon="el-icon-plus" @click="handleAdd">新增仓库</el-button>
      </div>

      <el-table v-loading="tableLoading" :data="tableData" border stripe>
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column prop="warehouseCode" label="仓库编码" min-width="120" />
        <el-table-column prop="warehouseName" label="仓库名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="manager" label="管理员" min-width="100" />
        <el-table-column prop="phone" label="联系电话" min-width="130" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right" class-name="col-action">
          <template slot-scope="{ row }">
            <el-button type="text" icon="el-icon-view" @click="handleView(row)">查看</el-button>
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" style="color:#f56c6c" icon="el-icon-delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          :current-page="query.page"
          :page-size="query.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- 新增/编辑/查看弹窗 -->
    <el-dialog :visible.sync="dialogVisible" :title="dialogTitle" width="560px" @close="handleDialogClose">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="仓库编码" prop="warehouseCode">
              <el-input v-model="form.warehouseCode" placeholder="请输入仓库编码" maxlength="50" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库名称" prop="warehouseName">
              <el-input v-model="form.warehouseName" placeholder="请输入仓库名称" maxlength="100" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="管理员" prop="manager">
              <el-input v-model="form.manager" placeholder="请输入管理员" maxlength="50" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="20" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="form.sort" :min="0" :max="9999" style="width:100%" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入地址" maxlength="200" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息" maxlength="200" :disabled="isView" />
            </el-form-item>
          </el-col>
        </el-row>
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
  getWarehousePage,
  addWarehouse,
  updateWarehouse,
  deleteWarehouse
} from '@/api/warehouse'

export default {
  name: 'Warehouse',
  data() {
    return {
      query: {
        page: 1,
        size: 10,
        warehouseName: '',
        warehouseCode: '',
        status: undefined
      },
      tableData: [],
      total: 0,
      tableLoading: false,
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      isView: false,
      form: this.buildEmptyForm(),
      rules: {
        warehouseCode: [{ required: true, message: '请输入仓库编码', trigger: 'blur' }],
        warehouseName: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    buildEmptyForm() {
      return {
        id: null,
        warehouseCode: '',
        warehouseName: '',
        address: '',
        manager: '',
        phone: '',
        sort: 0,
        status: 1,
        remark: ''
      }
    },
    async loadData() {
      this.tableLoading = true
      try {
        const params = {
          page: this.query.page,
          size: this.query.size,
          warehouseName: this.query.warehouseName || undefined,
          warehouseCode: this.query.warehouseCode || undefined,
          status: this.query.status === '' || this.query.status === undefined ? undefined : this.query.status
        }
        const res = await getWarehousePage(params)
        const data = res || {}
        this.tableData = data.records || []
        this.total = data.total || 0
      } catch (e) {
        // 错误已由拦截器弹出
      } finally {
        this.tableLoading = false
      }
    },
    handleSearch() {
      this.query.page = 1
      this.loadData()
    },
    handleReset() {
      this.query.warehouseName = ''
      this.query.warehouseCode = ''
      this.query.status = undefined
      this.query.page = 1
      this.loadData()
    },
    handleSizeChange(size) {
      this.query.size = size
      this.query.page = 1
      this.loadData()
    },
    handleCurrentChange(page) {
      this.query.page = page
      this.loadData()
    },
    handleAdd() {
      this.openDialog('add')
    },
    handleEdit(row) {
      this.openDialog('edit', row)
    },
    handleView(row) {
      this.openDialog('view', row)
    },
    /**
     * 打开弹窗
     * @param mode 'add' | 'edit' | 'view'
     * @param row 编辑/查看时为当前行
     */
    openDialog(mode, row) {
      this.isEdit = mode === 'edit'
      this.isView = mode === 'view'
      const titleMap = { add: '新增仓库', edit: '编辑仓库', view: '查看仓库' }
      this.dialogTitle = titleMap[mode]
      if (mode === 'add') {
        this.form = this.buildEmptyForm()
      } else {
        this.form = { ...this.buildEmptyForm(), ...row }
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.clearValidate()
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该条记录?', '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deleteWarehouse(row.id)
            this.$message.success('操作成功')
            this.loadData()
          } catch (e) {
            // 错误已由拦截器弹出
          }
        })
        .catch(() => {})
    },
    handleSubmit() {
      // 查看模式禁止提交
      if (this.isView) return
      this.$refs.form.validate(async valid => {
        if (!valid) return
        const payload = { ...this.form }
        try {
          if (this.isEdit) {
            await updateWarehouse(payload)
          } else {
            await addWarehouse(payload)
          }
          this.$message.success('操作成功')
          this.dialogVisible = false
          this.loadData()
        } catch (e) {
          // 错误已由拦截器弹出
        }
      })
    },
    handleDialogClose() {
      this.form = this.buildEmptyForm()
      this.isView = false
      this.$refs.form && this.$refs.form.clearValidate()
    }
  }
}
</script>

<style lang="scss" scoped>
.dialog-footer {
  text-align: right;
}
/* 操作列按钮不换行 */
::v-deep .col-action .cell {
  white-space: nowrap;
}
</style>
