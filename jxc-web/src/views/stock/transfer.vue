<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :inline="true" :model="query" size="small" class="search-form">
      <el-form-item label="单据编号">
        <el-input v-model="query.orderNo" placeholder="请输入单据编号" clearable @keyup.enter.native="handleSearch" />
      </el-form-item>
      <el-form-item label="调出仓库">
        <el-select v-model="query.fromWarehouseId" placeholder="全部" clearable filterable>
          <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="调入仓库">
        <el-select v-model="query.toWarehouseId" placeholder="全部" clearable filterable>
          <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="出库状态">
        <el-select v-model="query.auditStatus" placeholder="全部" clearable style="width:120px">
          <el-option label="待出库" :value="0" />
          <el-option label="已出库" :value="1" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 列表 -->
    <div class="table-toolbar">
      <el-button type="success" icon="el-icon-plus" @click="handleAdd">新增</el-button>
      <el-button type="warning" icon="el-icon-download" :loading="exporting" @click="handleExport">导出Excel</el-button>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="orderNo" label="单据编号" min-width="160" />
      <el-table-column label="调出仓库" min-width="120">
        <template slot-scope="{ row }">{{ getWarehouseName(row.fromWarehouseId) }}</template>
      </el-table-column>
      <el-table-column label="调入仓库" min-width="120">
        <template slot-scope="{ row }">{{ getWarehouseName(row.toWarehouseId) }}</template>
      </el-table-column>
      <el-table-column label="调拨商品" min-width="260">
        <template slot-scope="{ row }">
          <div v-if="row.details && row.details.length" class="product-tags">
            <el-tag v-for="(d, idx) in row.details" :key="idx" size="mini" effect="plain" class="product-tag">
              {{ d.productName || '-' }} ×{{ d.quantity }}
            </el-tag>
          </div>
          <span v-else class="empty-tip">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="orderTime" label="单据日期" min-width="160" />
      <el-table-column prop="totalQuantity" label="总数量" width="90" align="right" />
      <el-table-column label="出库状态" width="100" align="center">
        <template slot-scope="{ row }">
          <el-tag :type="row.auditStatus === 1 ? 'success' : 'warning'" size="small">
            {{ row.auditStatus === 1 ? '已出库' : '待出库' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right" align="center">
        <template slot-scope="{ row }">
          <el-button type="text" icon="el-icon-view" @click="handleView(row)">查看</el-button>
          <template v-if="row.auditStatus !== 1">
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(row)">删除</el-button>
            <el-button type="text" icon="el-icon-top" style="color:#67C23A" @click="handleAudit(row)">出库</el-button>
          </template>
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
      class="pagination"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />

    <!-- 新增/编辑/查看 弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="80%"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form ref="transferForm" :model="form" :rules="rules" :disabled="formDisabled" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="单据编号">
              <el-input v-model="form.orderNo" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="调出仓库" prop="fromWarehouseId">
              <el-select v-model="form.fromWarehouseId" placeholder="请选择调出仓库" filterable style="width:100%">
                <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="调入仓库" prop="toWarehouseId">
              <el-select v-model="form.toWarehouseId" placeholder="请选择调入仓库" filterable style="width:100%">
                <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="单据日期" prop="orderTime">
              <el-date-picker
                v-model="form.orderTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="选择日期"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="调拨原因">
          <el-input v-model="form.remark" placeholder="请输入调拨原因" maxlength="200" show-word-limit />
        </el-form-item>

        <!-- 明细 -->
        <el-form-item label="明细列表" label-width="100px"></el-form-item>
        <el-table :data="form.details" border stripe size="small">
          <el-table-column type="index" label="序号" width="50" align="center" />
          <el-table-column label="商品" min-width="220">
            <template slot-scope="{ row }">
              <el-select
                v-model="row.productId"
                placeholder="请选择商品"
                filterable
                style="width:100%"
                @change="(val) => handleProductChange(row, val)"
              >
                <el-option v-for="p in productList" :key="p.id" :label="p.productName" :value="p.id" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="productCode" label="商品编码" width="120" />
          <el-table-column prop="spec" label="规格" width="120" />
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column label="数量" width="160">
            <template slot-scope="{ row }">
              <el-input-number v-model="row.quantity" :min="0" :precision="0" :step="1" controls-position="right" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="160">
            <template slot-scope="{ row }">
              <el-input v-model="row.remark" placeholder="备注" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center" fixed="right">
            <template slot-scope="{ $index }">
              <el-button type="text" icon="el-icon-delete" style="color:#F56C6C" :disabled="formDisabled" @click="removeDetail($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="detail-toolbar">
          <el-button type="primary" plain icon="el-icon-plus" size="small" :disabled="formDisabled" @click="addDetail">添加明细</el-button>
          <div class="detail-summary">
            <span class="summary-item">商品种类: <b>{{ detailCount }}</b></span>
            <span class="summary-item">商品总数量: <b class="amount">{{ totalQuantity }}</b></span>
          </div>
        </div>
      </el-form>

      <div slot="footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="!formDisabled" type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getStockTransferPage,
  getStockTransfer,
  addStockTransfer,
  updateStockTransfer,
  deleteStockTransfer,
  auditStockTransfer,
  exportStockTransfers
} from '@/api/stock'
import { getAllProducts } from '@/api/product'
import { getAllWarehouses } from '@/api/warehouse'
import * as XLSX from 'xlsx'

export default {
  name: 'StockTransfer',
  data() {
    return {
      loading: false,
      submitting: false,
      exporting: false,
      list: [],
      total: 0,
      query: { page: 1, size: 10, orderNo: '', fromWarehouseId: undefined, toWarehouseId: undefined, auditStatus: undefined },
      warehouseList: [],
      productList: [],
      dialogVisible: false,
      dialogType: 'add',
      form: this.getEmptyForm(),
      rules: {
        fromWarehouseId: [{ required: true, message: '请选择调出仓库', trigger: 'change' }],
        toWarehouseId: [{ required: true, message: '请选择调入仓库', trigger: 'change' }],
        orderTime: [{ required: true, message: '请选择单据日期', trigger: 'change' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      const map = { add: '新增调拨单', edit: '编辑调拨单', view: '查看调拨单' }
      return map[this.dialogType] || '调拨单'
    },
    formDisabled() {
      return this.dialogType === 'view'
    },
    // 已选商品种类(按商品去重)
    detailCount() {
      const ids = (this.form.details || []).map(d => d.productId).filter(id => id !== undefined && id !== null && id !== '')
      return new Set(ids).size
    },
    // 明细数量合计
    totalQuantity() {
      return (this.form.details || []).reduce((s, r) => s + (Number(r.quantity) || 0), 0)
    }
  },
  created() {
    this.loadOptions()
    this.loadList()
  },
  activated() {
    // 页面被 keep-alive 缓存,再次激活时刷新基础数据(商品/仓库等可能已在其他页面修改)
    this.loadOptions()
  },
  methods: {
    getEmptyForm() {
      return {
        id: undefined,
        orderNo: '',
        fromWarehouseId: undefined,
        toWarehouseId: undefined,
        orderTime: this.nowDateTime(),
        remark: '',
        details: []
      }
    },
    nowDateTime() {
      const d = new Date()
      const pad = n => (n < 10 ? '0' + n : '' + n)
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    },
    async loadOptions() {
      try {
        const [warehouses, products] = await Promise.all([
          getAllWarehouses(),
          getAllProducts()
        ])
        this.warehouseList = warehouses || []
        this.productList = products || []
      } catch (e) { /* 忽略 */ }
    },
    async loadList() {
      this.loading = true
      try {
        const data = await getStockTransferPage(this.query)
        this.list = (data && data.records) || []
        this.total = (data && data.total) || 0
      } catch (e) { /* 忽略 */ } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.query.page = 1
      this.loadList()
    },
    handleReset() {
      this.query = { page: 1, size: 10, orderNo: '', fromWarehouseId: undefined, toWarehouseId: undefined, auditStatus: undefined }
      this.loadList()
    },
    handleSizeChange(size) {
      this.query.size = size
      this.loadList()
    },
    handlePageChange(page) {
      this.query.page = page
      this.loadList()
    },
    getWarehouseName(id) {
      const w = this.warehouseList.find(i => i.id === id)
      return w ? w.warehouseName : '-'
    },
    handleAdd() {
      this.dialogType = 'add'
      this.form = this.getEmptyForm()
      this.addDetail()
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.transferForm && this.$refs.transferForm.clearValidate())
    },
    async handleView(row) {
      this.dialogType = 'view'
      await this.loadDetail(row.id)
      this.dialogVisible = true
    },
    async handleEdit(row) {
      this.dialogType = 'edit'
      await this.loadDetail(row.id)
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.transferForm && this.$refs.transferForm.clearValidate())
    },
    async loadDetail(id) {
      try {
        const data = await getStockTransfer(id)
        this.form = {
          id: data.id,
          orderNo: data.orderNo || '',
          fromWarehouseId: data.fromWarehouseId,
          toWarehouseId: data.toWarehouseId,
          orderTime: data.orderTime,
          remark: data.remark || '',
          details: (data.details || []).map(d => ({ ...d }))
        }
      } catch (e) { /* 忽略 */ }
    },
    handleDelete(row) {
      this.$confirm(`确认删除调拨单「${row.orderNo}」吗?`, '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deleteStockTransfer(row.id)
            this.$message.success('删除成功')
            this.loadList()
          } catch (e) { /* 忽略 */ }
        }).catch(() => {})
    },
    handleAudit(row) {
      this.$prompt('出库后单据不可修改,且将执行库存调拨(扣减源仓库、增加目标仓库),确认出库?', '提示', {
        type: 'warning',
        inputPlaceholder: '可填写出库备注(选填)',
        inputType: 'textarea'
      }).then(async ({ value }) => {
        try {
          await auditStockTransfer(row.id, { remark: value || '' })
          this.$message.success('出库成功')
          this.loadList()
        } catch (e) { /* 忽略 */ }
      }).catch(() => {})
    },
    /** 导出 Excel: 两张 sheet, 调拨单汇总 + 调拨明细 */
    async handleExport() {
      if (this.exporting) return
      this.exporting = true
      try {
        // 使用当前搜索条件(不带分页参数)查询全部匹配数据
        const params = {
          orderNo: this.query.orderNo,
          fromWarehouseId: this.query.fromWarehouseId,
          toWarehouseId: this.query.toWarehouseId,
          auditStatus: this.query.auditStatus
        }
        const orders = await exportStockTransfers(params)
        if (!orders || orders.length === 0) {
          this.$message.warning('暂无数据可导出')
          return
        }

        // ===== Sheet1: 调拨单汇总 =====
        const headerTitle = [['库存调拨单汇总']]
        const headerCols = ['序号', '单据编号', '调出仓库', '调入仓库', '单据日期', '商品明细', '总数量', '出库状态', '出库时间', '备注']
        const headerData = orders.map((o, i) => [
          i + 1,
          o.orderNo,
          this.getWarehouseName(o.fromWarehouseId),
          this.getWarehouseName(o.toWarehouseId),
          o.orderTime || '',
          (o.details || []).map(d => `${d.productName || '-'}×${d.quantity || 0}${d.unit || ''}`).join(', '),
          o.totalQuantity != null ? o.totalQuantity : 0,
          o.auditStatus === 1 ? '已出库' : '待出库',
          o.auditTime || '',
          o.remark || ''
        ])
        const headerRows = [...headerTitle, headerCols, ...headerData]

        // ===== Sheet2: 调拨明细 =====
        const detailTitle = [['库存调拨明细']]
        const detailCols = ['序号', '单据编号', '调出仓库', '调入仓库', '商品编码', '商品名称', '规格', '单位', '数量', '备注']
        const detailRows = []
        let idx = 1
        orders.forEach(o => {
          const details = o.details || []
          if (details.length === 0) {
            // 无明细也保留一行主表信息
            detailRows.push([
              idx++, o.orderNo, this.getWarehouseName(o.fromWarehouseId), this.getWarehouseName(o.toWarehouseId),
              '', '', '', '', '', ''
            ])
          } else {
            details.forEach(d => {
              detailRows.push([
                idx++, o.orderNo, this.getWarehouseName(o.fromWarehouseId), this.getWarehouseName(o.toWarehouseId),
                d.productCode || '', d.productName || '', d.spec || '', d.unit || '',
                d.quantity != null ? d.quantity : 0,
                d.remark || ''
              ])
            })
          }
        })
        const allDetailRows = [...detailTitle, detailCols, ...detailRows]

        // ===== 构建 Workbook =====
        const wb = XLSX.utils.book_new()
        const wsHeader = XLSX.utils.aoa_to_sheet(headerRows)
        const wsDetail = XLSX.utils.aoa_to_sheet(allDetailRows)

        // 合并标题行
        wsHeader['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: headerCols.length - 1 } }]
        wsDetail['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: detailCols.length - 1 } }]

        // 列宽
        wsHeader['!cols'] = [
          { wch: 6 }, { wch: 22 }, { wch: 14 }, { wch: 14 }, { wch: 20 },
          { wch: 40 }, { wch: 10 }, { wch: 10 }, { wch: 20 }, { wch: 20 }
        ]
        wsDetail['!cols'] = [
          { wch: 6 }, { wch: 22 }, { wch: 14 }, { wch: 14 }, { wch: 14 },
          { wch: 18 }, { wch: 12 }, { wch: 8 }, { wch: 10 }, { wch: 20 }
        ]

        XLSX.utils.book_append_sheet(wb, wsHeader, '调拨单汇总')
        XLSX.utils.book_append_sheet(wb, wsDetail, '调拨明细')

        const ts = this.nowDateString()
        XLSX.writeFile(wb, `库存调拨单_${ts}.xlsx`)
        this.$message.success(`导出成功,共 ${orders.length} 张单据`)
      } catch (e) {
        console.error(e)
        this.$message.error('导出失败: ' + (e.message || '未知错误'))
      } finally {
        this.exporting = false
      }
    },
    /** yyyyMMdd_HHmmss 文件名时间戳 */
    nowDateString() {
      const d = new Date()
      const pad = n => (n < 10 ? '0' + n : '' + n)
      return `${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}_${pad(d.getHours())}${pad(d.getMinutes())}${pad(d.getSeconds())}`
    },
    addDetail() {
      this.form.details.push({
        productId: undefined,
        productCode: '',
        productName: '',
        spec: '',
        unit: '',
        quantity: 1,
        remark: ''
      })
    },
    removeDetail(idx) {
      this.form.details.splice(idx, 1)
    },
    handleProductChange(row, val) {
      const p = this.productList.find(i => i.id === val)
      if (p) {
        row.productCode = p.productCode || ''
        row.productName = p.productName || ''
        row.spec = p.spec || ''
        row.unit = p.unit || ''
      }
    },
    handleSubmit() {
      this.$refs.transferForm.validate(async valid => {
        if (!valid) return
        if (this.form.fromWarehouseId === this.form.toWarehouseId) {
          this.$message.warning('调出仓库与调入仓库不能相同')
          return
        }
        if (!this.form.details || this.form.details.length === 0) {
          this.$message.warning('请至少添加一条明细')
          return
        }
        for (const d of this.form.details) {
          if (!d.productId) { this.$message.warning('请选择商品'); return }
          if (!d.quantity || d.quantity <= 0) { this.$message.warning('数量必须大于 0'); return }
        }
        this.form.totalQuantity = this.form.details.reduce((s, r) => s + (Number(r.quantity) || 0), 0)
        this.submitting = true
        try {
          if (this.dialogType === 'add') {
            await addStockTransfer(this.form)
            this.$message.success('新增成功')
          } else {
            await updateStockTransfer({ ...this.form })
            this.$message.success('修改成功')
          }
          this.dialogVisible = false
          this.loadList()
        } catch (e) { /* 忽略 */ } finally {
          this.submitting = false
        }
      })
    },
    handleDialogClose() {
      this.form = this.getEmptyForm()
      this.$nextTick(() => this.$refs.transferForm && this.$refs.transferForm.clearValidate())
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  .pagination { margin-top: 12px; text-align: right; }
}
/* 覆盖全局 search-form 最后一个 el-form-item 推右样式,使出库状态紧跟调入仓库,间距一致 */
::v-deep .search-form .el-form-item:last-child {
  margin-left: 0 !important;
}
/* 出库状态下拉框与查询按钮间距与按钮间默认间距(10px)保持一致 */
::v-deep .search-form .el-form-item .el-select + .el-button {
  margin-left: 10px;
}
.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  .product-tag { margin: 0; }
}
.empty-tip { color: #c0c4cc; }
/* 明细工具栏:左侧添加按钮,右侧合计 */
.detail-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.detail-summary {
  .summary-item {
    margin-left: 24px;
    font-size: 14px;
    color: #606266;
    b { font-size: 16px; color: #303133; margin-left: 4px; }
    .amount { color: #F56C6C; font-size: 18px; }
  }
}
</style>
