<template>
  <div class="app-container">
    <!-- 列表(含搜索区) -->
    <div class="table-card">
      <!-- 搜索条件 -->
      <el-form class="search-form" :inline="true" :model="query">
        <el-form-item label="商品名称">
          <el-input v-model="query.productName" placeholder="商品名称" clearable @keyup.enter.native="handleSearch" style="width:180px" />
        </el-form-item>
        <el-form-item label="商品编码">
          <el-input v-model="query.productCode" placeholder="商品编码" clearable @keyup.enter.native="handleSearch" style="width:180px" />
        </el-form-item>
        <el-form-item label="商品分类">
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
            <el-option label="停售" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜 索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重 置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-toolbar">
        <el-button type="success" icon="el-icon-plus" @click="handleAdd">新增商品</el-button>
      </div>
      <el-table v-loading="tableLoading" :data="tableData" border stripe>
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column prop="productCode" label="商品编码" min-width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="商品分类" min-width="140">
          <template slot-scope="{ row }">{{ formatCategory(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column prop="spec" label="规格" min-width="120" show-overflow-tooltip />
        <el-table-column label="单位" width="90" align="center">
          <template slot-scope="{ row }">
            {{ row.bigUnit ? (row.unit + '/' + row.bigUnit) : row.unit }}
          </template>
        </el-table-column>
        <el-table-column prop="barcode" label="条码" min-width="130" show-overflow-tooltip />
        <el-table-column prop="expireDate" label="保质期至" min-width="120" align="center">
          <template slot-scope="{ row }">{{ row.expireDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="成本价" width="100" align="right">
          <template slot-scope="{ row }">{{ formatMoney(row.costPrice) }}</template>
        </el-table-column>
        <el-table-column label="售价" width="100" align="right">
          <template slot-scope="{ row }">{{ formatMoney(row.salePrice) }}</template>
        </el-table-column>
        <el-table-column prop="minStock" label="最低库存" width="90" align="center" />
        <el-table-column prop="maxStock" label="最高库存" width="90" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停售' }}
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
    <el-dialog :visible.sync="dialogVisible" :title="dialogTitle" width="640px" @close="handleDialogClose">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="商品编码" prop="productCode">
              <el-input v-model="form.productCode" placeholder="请输入商品编码" maxlength="50" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="请输入商品名称" maxlength="100" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryPath">
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
            <el-form-item label="规格" prop="spec">
              <el-input v-model="form.spec" placeholder="请输入规格" maxlength="50" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="基本单位" prop="unit">
              <el-input v-model="form.unit" placeholder="最小单位,如: 瓶/个" maxlength="20" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="大单位" prop="bigUnit">
              <el-input v-model="form.bigUnit" placeholder="选填,如: 箱" maxlength="20" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col v-if="form.bigUnit" :span="12">
            <el-form-item label="换算率" prop="conversionRate">
              <el-input-number v-model="form.conversionRate" :min="2" :precision="0" :step="1" style="width:100%" :disabled="isView" />
              <div class="form-tip">1 {{ form.bigUnit || '大单位' }} = {{ form.conversionRate || '?' }} {{ form.unit || '基本单位' }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="条码" prop="barcode">
              <el-input v-model="form.barcode" placeholder="请输入条码" maxlength="50" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保质期至" prop="expireDate">
              <el-date-picker v-model="form.expireDate" type="date" value-format="yyyy-MM-dd" placeholder="选填,如 2027-01-01" :disabled="isView" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成本价" prop="costPrice">
              <el-input-number v-model="form.costPrice" :min="0" :precision="2" :step="0.01" style="width:100%" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="售价" prop="salePrice">
              <el-input-number v-model="form.salePrice" :min="0" :precision="2" :step="0.01" style="width:100%" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最低库存" prop="minStock">
              <el-input-number v-model="form.minStock" :min="0" :precision="0" style="width:100%" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高库存" prop="maxStock">
              <el-input-number v-model="form.maxStock" :min="0" :precision="0" style="width:100%" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停售" :disabled="isView" />
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
  getProductPage,
  addProduct,
  updateProduct,
  deleteProduct,
  getProductCategoryTree
} from '@/api/product'
import { formatMoney } from '@/utils/format'

export default {
  name: 'Product',
  data() {
    return {
      query: {
        page: 1,
        size: 10,
        productName: '',
        productCode: '',
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
      // 商品分类树
      categoryTree: [],
      // 分类扁平化后的 id -> name 映射,用于列表展示分类名称
      categoryMap: {},
      cascaderProps: {
        checkStrictly: true,
        value: 'id',
        label: 'categoryName',
        children: 'children',
        emitPath: true
      },
      form: this.buildEmptyForm(),
      rules: {
        productCode: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
        productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        categoryPath: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
        unit: [{ required: true, message: '请输入基本单位', trigger: 'blur' }],
        conversionRate: [{ validator: this.validateConversionRate, trigger: 'blur' }],
        costPrice: [{ required: true, message: '请输入成本价', trigger: 'blur' }],
        salePrice: [{ required: true, message: '请输入售价', trigger: 'blur' }]
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
    /** 金额格式化 */
    formatMoney(v) {
      return formatMoney(v)
    },
    /** 显示分类名称 */
    formatCategory(id) {
      return this.categoryMap[id] || (id ? '分类#' + id : '')
    },
    /** 构建空表单 */
    buildEmptyForm() {
      return {
        id: null,
        productCode: '',
        productName: '',
        categoryId: null,
        categoryPath: [],
        spec: '',
        unit: '',
        bigUnit: '',
        conversionRate: undefined,
        barcode: '',
        expireDate: '',
        costPrice: 0,
        salePrice: 0,
        minStock: 0,
        maxStock: 0,
        status: 1,
        remark: ''
      }
    },
    /** 换算率校验: 填了大单位时必填且≥2 */
    validateConversionRate(rule, value, callback) {
      if (this.form.bigUnit && (!value || value < 2)) {
        callback(new Error('填写大单位时,换算率必须≥2'))
      } else {
        callback()
      }
    },
    /** 加载分类树并构建 id->name 映射 */
    async loadCategoryTree() {
      try {
        const data = await getProductCategoryTree()
        this.categoryTree = Array.isArray(data) ? data : []
        this.categoryMap = {}
        this.flattenTree(this.categoryTree)
      } catch (e) {
        // 错误已由拦截器弹出
      }
    },
    /** 扁平化分类树,生成 id->categoryName 映射 */
    flattenTree(tree) {
      (tree || []).forEach(node => {
        this.categoryMap[node.id] = node.categoryName
        if (node.children && node.children.length) {
          this.flattenTree(node.children)
        }
      })
    },
    /** 加载列表数据 */
    async loadData() {
      this.tableLoading = true
      try {
        const params = {
          page: this.query.page,
          size: this.query.size,
          productName: this.query.productName || undefined,
          productCode: this.query.productCode || undefined,
          status: this.query.status === '' || this.query.status === undefined ? undefined : this.query.status,
          categoryId: this.query.categoryPath && this.query.categoryPath.length
            ? this.query.categoryPath[this.query.categoryPath.length - 1]
            : undefined
        }
        const res = await getProductPage(params)
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
      this.query.productName = ''
      this.query.productCode = ''
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
      const titleMap = { add: '新增商品', edit: '编辑商品', view: '查看商品' }
      this.dialogTitle = titleMap[action]
      if (action === 'add') {
        this.form = this.buildEmptyForm()
      } else {
        this.form = { ...this.buildEmptyForm(), ...row }
        // 由 categoryId 反推级联路径
        this.form.categoryPath = this.findPathInTree(this.categoryTree, row.categoryId) || []
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
            await deleteProduct(row.id)
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
        // 由级联路径计算 categoryId
        payload.categoryId = payload.categoryPath && payload.categoryPath.length
          ? payload.categoryPath[payload.categoryPath.length - 1]
          : null
        delete payload.categoryPath
        // 保质期至: 未选择时置空,避免传空字符串给后端
        if (!payload.expireDate) payload.expireDate = null
        // 未填大单位时清空换算率
        if (!payload.bigUnit) {
          payload.conversionRate = null
        }
        try {
          if (this.isEdit) {
            await updateProduct(payload)
          } else {
            await addProduct(payload)
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
      this.$refs.form && this.$refs.form.clearValidate()
    },
    /** 在树中递归查找指定 id 的路径(返回 id 数组) */
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
/* 换算率提示文字 */
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  margin-top: 2px;
}
/* 列表工具栏: 标题左、按钮右 */
.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}
.toolbar-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
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
