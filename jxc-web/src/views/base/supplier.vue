<template>
  <div class="app-container">
    <!-- 列表(含搜索区) -->
    <div class="table-card">
      <!-- 搜索条件 -->
      <el-form class="search-form" :inline="true" :model="query">
        <el-form-item label="供应商名称">
          <el-input v-model="query.supplierName" placeholder="供应商名称" clearable @keyup.enter.native="handleSearch" style="width:180px" />
        </el-form-item>
        <el-form-item label="供应商编码">
          <el-input v-model="query.supplierCode" placeholder="供应商编码" clearable @keyup.enter.native="handleSearch" style="width:180px" />
        </el-form-item>
        <el-form-item label="供应商分类">
          <el-cascader
            ref="queryCascader"
            v-model="query.categoryPath"
            :options="categoryTree"
            :props="cascaderProps"
            clearable
            filterable
            placeholder="请选择分类"
            style="width:200px"
            @change="closeCascader('queryCascader')"
          />
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
        <el-button type="success" icon="el-icon-plus" @click="handleAdd">新增供应商</el-button>
      </div>

      <el-table v-loading="tableLoading" :data="tableData" border stripe>
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column prop="supplierCode" label="供应商编码" min-width="120" />
        <el-table-column prop="supplierName" label="供应商名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="分类" min-width="120">
          <template slot-scope="{ row }">{{ formatCategory(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" min-width="100" />
        <el-table-column prop="contactPhone" label="联系电话" min-width="130" />
        <el-table-column label="等级" width="90" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="levelTagType(row.level)">{{ levelText(row.level) }}</el-tag>
          </template>
        </el-table-column>
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
    <el-dialog :visible.sync="dialogVisible" :title="dialogTitle" :width="isView ? '900px' : '680px'" @close="handleDialogClose">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="供应商编码" prop="supplierCode">
              <el-input v-model="form.supplierCode" placeholder="请输入供应商编码" maxlength="50" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商名称" prop="supplierName">
              <el-input v-model="form.supplierName" placeholder="请输入供应商名称" maxlength="100" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商分类" prop="categoryPath">
              <el-cascader
                ref="formCascader"
                v-model="form.categoryPath"
                :options="categoryTree"
                :props="cascaderProps"
                clearable
                filterable
                placeholder="请选择分类"
                style="width:100%"
                :disabled="isView"
                @change="closeCascader('formCascader')"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="等级" prop="level">
              <el-select v-model="form.level" placeholder="请选择等级" style="width:100%" :disabled="isView">
                <el-option label="普通" :value="1" />
                <el-option label="优质" :value="2" />
                <el-option label="战略" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="form.contactPerson" placeholder="请输入联系人" maxlength="50" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" maxlength="20" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开户行" prop="bankName">
              <el-input v-model="form.bankName" placeholder="请输入开户行" maxlength="50" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="银行账号" prop="bankAccount">
              <el-input v-model="form.bankAccount" placeholder="请输入银行账号" maxlength="50" :disabled="isView" />
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

      <!-- 查看模式: 关联单据(采购入库单、采购退货单) -->
      <div v-if="isView" v-loading="relationsLoading" class="relations-block">
        <div class="relations-title">采购入库单 ({{ purchaseOrders.length }})</div>
        <el-table :data="purchaseOrders" border stripe size="mini" max-height="260">
          <el-table-column type="index" label="序号" width="55" align="center" />
          <el-table-column prop="orderNo" label="单据编号" min-width="140" show-overflow-tooltip />
          <el-table-column prop="orderTime" label="采购日期" min-width="150" />
          <el-table-column prop="totalQuantity" label="总数量" width="90" align="center" />
          <el-table-column prop="totalAmount" label="总金额" width="110" align="right" />
          <el-table-column label="入库状态" width="100" align="center">
            <template slot-scope="{ row }">
              <el-tag :type="row.auditStatus === 1 ? 'success' : 'warning'" size="mini">
                {{ row.auditStatus === 1 ? '已入库' : '待入库' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div class="relations-title" style="margin-top:18px">采购退货单 ({{ purchaseReturnOrders.length }})</div>
        <el-table :data="purchaseReturnOrders" border stripe size="mini" max-height="260">
          <el-table-column type="index" label="序号" width="55" align="center" />
          <el-table-column prop="orderNo" label="退货单号" min-width="140" show-overflow-tooltip />
          <el-table-column prop="orderTime" label="退货日期" min-width="150" />
          <el-table-column prop="totalQuantity" label="总数量" width="90" align="center" />
          <el-table-column prop="totalAmount" label="总金额" width="110" align="right" />
          <el-table-column prop="returnReason" label="退货原因" min-width="140" show-overflow-tooltip />
          <el-table-column label="出库状态" width="100" align="center">
            <template slot-scope="{ row }">
              <el-tag :type="row.auditStatus === 1 ? 'success' : 'warning'" size="mini">
                {{ row.auditStatus === 1 ? '已出库' : '待出库' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">{{ isView ? '关 闭' : '取 消' }}</el-button>
        <el-button v-if="!isView" type="primary" @click="handleSubmit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getSupplierPage,
  addSupplier,
  updateSupplier,
  deleteSupplier,
  getSupplierCategoryTree
} from '@/api/supplier'
import {
  getPurchaseOrderPage,
  getPurchaseReturnPage
} from '@/api/purchase'

export default {
  name: 'Supplier',
  data() {
    return {
      query: {
        page: 1,
        size: 10,
        supplierName: '',
        supplierCode: '',
        categoryPath: [],
        status: undefined
      },
      tableData: [],
      total: 0,
      tableLoading: false,
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      isView: false,
      categoryTree: [],
      categoryMap: {},
      cascaderProps: {
        checkStrictly: true,
        value: 'id',
        label: 'categoryName',
        children: 'children',
        emitPath: true
      },
      form: this.buildEmptyForm(),
      // 查看模式: 关联单据
      purchaseOrders: [],
      purchaseReturnOrders: [],
      relationsLoading: false,
      rules: {
        supplierCode: [{ required: true, message: '请输入供应商编码', trigger: 'blur' }],
        supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
        categoryPath: [{ required: true, message: '请选择供应商分类', trigger: 'change' }],
        level: [{ required: true, message: '请选择等级', trigger: 'change' }]
      }
    }
  },
  created() {
    this.loadCategoryTree()
    this.loadData()
  },
  methods: {
    /** 分类选中后自动收起下拉面板(checkStrictly 模式下不会自动关闭) */
    closeCascader(refName) {
      const cascader = this.$refs[refName]
      if (cascader) cascader.dropDownVisible = false
    },
    /** 显示分类名称 */
    formatCategory(id) {
      return this.categoryMap[id] || (id ? '分类#' + id : '')
    },
    /** 等级文本 */
    levelText(level) {
      const map = { 1: '普通', 2: '优质', 3: '战略' }
      return map[level] || '-'
    },
    /** 等级标签颜色 */
    levelTagType(level) {
      const map = { 1: 'info', 2: '', 3: 'success' }
      return map[level] || 'info'
    },
    buildEmptyForm() {
      return {
        id: null,
        supplierCode: '',
        supplierName: '',
        categoryId: null,
        categoryPath: [],
        contactPerson: '',
        contactPhone: '',
        email: '',
        address: '',
        bankAccount: '',
        bankName: '',
        level: 1,
        status: 1,
        remark: ''
      }
    },
    async loadCategoryTree() {
      try {
        const data = await getSupplierCategoryTree()
        this.categoryTree = Array.isArray(data) ? data : []
        this.categoryMap = {}
        this.flattenTree(this.categoryTree)
      } catch (e) {
        // 错误已由拦截器弹出
      }
    },
    flattenTree(tree) {
      (tree || []).forEach(node => {
        this.categoryMap[node.id] = node.categoryName
        if (node.children && node.children.length) {
          this.flattenTree(node.children)
        }
      })
    },
    async loadData() {
      this.tableLoading = true
      try {
        const params = {
          page: this.query.page,
          size: this.query.size,
          supplierName: this.query.supplierName || undefined,
          supplierCode: this.query.supplierCode || undefined,
          status: this.query.status === '' || this.query.status === undefined ? undefined : this.query.status,
          categoryId: this.query.categoryPath && this.query.categoryPath.length
            ? this.query.categoryPath[this.query.categoryPath.length - 1]
            : undefined
        }
        const res = await getSupplierPage(params)
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
      this.query.supplierName = ''
      this.query.supplierCode = ''
      this.query.categoryPath = []
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
     * @param action 'add' | 'edit' | 'view'
     * @param row 编辑/查看时为当前行
     */
    openDialog(action, row) {
      this.isEdit = action === 'edit'
      this.isView = action === 'view'
      const titleMap = { add: '新增供应商', edit: '编辑供应商', view: '查看供应商' }
      this.dialogTitle = titleMap[action]
      if (action === 'add') {
        this.form = this.buildEmptyForm()
      } else {
        this.form = { ...this.buildEmptyForm(), ...row }
        this.form.categoryPath = this.findPathInTree(this.categoryTree, row.categoryId) || []
      }
      // 重置关联单据
      this.purchaseOrders = []
      this.purchaseReturnOrders = []
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.clearValidate()
      })
      // 查看模式: 加载关联的采购入库单与采购退货单
      if (action === 'view' && row && row.id) {
        this.loadRelations(row.id)
      }
    },
    /** 加载供应商关联的采购入库单和采购退货单 */
    async loadRelations(supplierId) {
      this.relationsLoading = true
      try {
        const [purchaseRes, returnRes] = await Promise.all([
          getPurchaseOrderPage({ page: 1, size: 100, supplierId }),
          getPurchaseReturnPage({ page: 1, size: 100, supplierId })
        ])
        this.purchaseOrders = (purchaseRes && purchaseRes.records) || []
        this.purchaseReturnOrders = (returnRes && returnRes.records) || []
      } catch (e) {
        // 错误已由拦截器弹出
      } finally {
        this.relationsLoading = false
      }
    },
    handleDelete(row) {
      this.$confirm('确认删除该条记录?', '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deleteSupplier(row.id)
            this.$message.success('操作成功')
            this.loadData()
          } catch (e) {
            // 错误已由拦截器弹出
          }
        })
        .catch(() => {})
    },
    handleSubmit() {
      if (this.isView) return
      this.$refs.form.validate(async valid => {
        if (!valid) return
        const payload = { ...this.form }
        payload.categoryId = payload.categoryPath && payload.categoryPath.length
          ? payload.categoryPath[payload.categoryPath.length - 1]
          : null
        delete payload.categoryPath
        try {
          if (this.isEdit) {
            await updateSupplier(payload)
          } else {
            await addSupplier(payload)
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
      this.purchaseOrders = []
      this.purchaseReturnOrders = []
      this.relationsLoading = false
      this.$refs.form && this.$refs.form.clearValidate()
    },
    findPathInTree(tree, id, path = []) {
      if (!id) return null
      for (const node of tree || []) {
        const currentPath = [...path, node.id]
        if (node.id === id) return currentPath
        if (node.children && node.children.length) {
          const found = this.findPathInTree(node.children, id, currentPath)
          if (found) return found
        }
      }
      return null
    }
  }
}
</script>

<style lang="scss" scoped>
.dialog-footer {
  text-align: right;
}
/* 操作列强制不换行 */
::v-deep .col-action,
::v-deep .col-action .cell {
  white-space: nowrap !important;
}
/* 关联单据区块 */
.relations-block {
  margin-top: 12px;
  border-top: 1px dashed #dcdfe6;
  padding-top: 14px;
}
.relations-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  border-left: 3px solid #409eff;
  padding-left: 8px;
}
</style>
