<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :inline="true" :model="query" size="small" class="search-bar search-form">
      <el-form-item label="退货单号">
        <el-input v-model="query.orderNo" placeholder="请输入退货单号" clearable @keyup.enter.native="handleSearch" style="width:180px" />
      </el-form-item>
      <el-form-item label="供应商">
        <el-select v-model="query.supplierId" placeholder="全部" clearable filterable style="width:180px">
          <el-option v-for="item in supplierList" :key="item.id" :label="item.supplierName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="仓库">
        <el-select v-model="query.warehouseId" placeholder="全部" clearable filterable style="width:160px">
          <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="出库状态">
        <el-select v-model="query.auditStatus" placeholder="全部" clearable style="width:120px">
          <el-option label="待出库" :value="0" />
          <el-option label="已出库" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="success" icon="el-icon-plus" @click="handleAdd">新增</el-button>
        <el-button type="warning" icon="el-icon-download" :loading="exporting" @click="handleExport">导出Excel</el-button>
      </el-form-item>
    </el-form>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="orderNo" label="退货单号" min-width="160" />
      <el-table-column label="供应商" min-width="160">
        <template slot-scope="{ row }">{{ getSupplierName(row.supplierId) }}</template>
      </el-table-column>
      <el-table-column label="仓库" min-width="120">
        <template slot-scope="{ row }">{{ getWarehouseName(row.warehouseId) }}</template>
      </el-table-column>
      <el-table-column label="退货商品" min-width="260">
        <template slot-scope="{ row }">
          <div v-if="row.details && row.details.length" class="product-tags">
            <el-tag v-for="(d, idx) in row.details" :key="idx" size="mini" effect="plain" class="product-tag">
              {{ d.productName || '-' }} ×{{ d.inputQuantity || d.quantity }}{{ d.unit || '' }}
            </el-tag>
          </div>
          <span v-else class="empty-tip">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="totalQuantity" label="总数量" width="90" align="right" />
      <el-table-column prop="totalAmount" label="总金额" width="110" align="right">
        <template slot-scope="{ row }">{{ formatMoney(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column prop="returnReason" label="退货原因" min-width="160" show-overflow-tooltip />
      <el-table-column prop="orderTime" label="退货日期" min-width="160" />
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
      <el-form ref="orderForm" :model="form" :rules="rules" :disabled="formDisabled" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="退货单号">
              <el-input v-model="form.orderNo" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" placeholder="请选择供应商" filterable style="width:100%">
                <el-option v-for="item in supplierList" :key="item.id" :label="item.supplierName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="出库仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" placeholder="请选择仓库" filterable style="width:100%">
                <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="退货日期" prop="orderTime">
              <el-date-picker
                v-model="form.orderTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="选择退货日期"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="退货原因" prop="returnReason">
              <el-input v-model="form.returnReason" placeholder="请输入退货原因" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="请输入备注" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 明细 -->
        <el-form-item label="退货明细" label-width="100px"></el-form-item>
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
          <el-table-column label="单位" width="110" align="center">
            <template slot-scope="{ row }">
              <el-select v-model="row.unit" size="small" :disabled="formDisabled" @change="() => handleUnitChange(row)">
                <el-option v-for="u in unitOptions(row)" :key="u" :label="u" :value="u" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="140">
            <template slot-scope="{ row }">
              <el-input-number v-model="row.inputQuantity" :min="0" :precision="0" :step="1" controls-position="right" size="small" @change="calcRow(row)" />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="140">
            <template slot-scope="{ row }">
              <el-input-number v-model="row.price" :min="0" :precision="2" :step="0.01" controls-position="right" size="small" @change="calcRow(row)" />
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120" align="right">
            <template slot-scope="{ row }">{{ formatMoney(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="备注" min-width="150">
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
            <span class="summary-item">商品种类：<b>{{ detailCount }}</b></span>
            <span class="summary-item">合计数量：<b>{{ totalQuantity }}</b></span>
            <span class="summary-item summary-amount">合计金额：<b class="amount">{{ formatMoney(totalAmount) }}</b></span>
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
  getPurchaseReturnPage,
  getPurchaseReturn,
  addPurchaseReturn,
  updatePurchaseReturn,
  deletePurchaseReturn,
  auditPurchaseReturn,
  exportPurchaseReturns
} from '@/api/purchase'
import { getAllProducts } from '@/api/product'
import { getAllWarehouses } from '@/api/warehouse'
import { getAllSuppliers } from '@/api/supplier'
import { formatMoney } from '@/utils/format'
import * as XLSX from 'xlsx'

export default {
  name: 'PurchaseReturn',
  data() {
    return {
      loading: false,
      exporting: false,
      submitting: false,
      list: [],
      total: 0,
      query: { page: 1, size: 10, orderNo: '', supplierId: undefined, warehouseId: undefined, auditStatus: undefined },
      supplierList: [],
      warehouseList: [],
      productList: [],
      dialogVisible: false,
      dialogType: 'add',
      form: this.getEmptyForm(),
      rules: {
        supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
        warehouseId: [{ required: true, message: '请选择出库仓库', trigger: 'change' }],
        orderTime: [{ required: true, message: '请选择退货日期', trigger: 'change' }],
        returnReason: [{ required: true, message: '请填写退货原因', trigger: 'blur' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      const map = { add: '新增采购退货单', edit: '编辑采购退货单', view: '查看采购退货单' }
      return map[this.dialogType] || '采购退货单'
    },
    formDisabled() {
      return this.dialogType === 'view'
    },
    /** 明细商品种类数(去重) */
    detailCount() {
      const ids = (this.form.details || []).map(d => d.productId).filter(id => id)
      return new Set(ids).size
    },
    /** 合计数量 */
    totalQuantity() {
      return (this.form.details || []).reduce((s, r) => s + (Number(r.quantity) || 0), 0)
    },
    /** 合计金额 */
    totalAmount() {
      return Number((this.form.details || []).reduce((s, r) => s + (Number(r.amount) || 0), 0).toFixed(2))
    }
  },
  created() {
    this.loadOptions()
    this.loadList()
  },
  activated() {
    // 页面被 keep-alive 缓存,再次激活时刷新基础数据(商品单价等可能已在其他页面修改)
    this.loadOptions()
  },
  methods: {
    formatMoney,
    getEmptyForm() {
      return {
        id: undefined,
        orderNo: '',
        supplierId: undefined,
        warehouseId: undefined,
        orderTime: this.nowDateTime(),
        returnReason: '',
        operatorId: undefined,
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
        const [suppliers, warehouses, products] = await Promise.all([
          getAllSuppliers(),
          getAllWarehouses(),
          getAllProducts()
        ])
        this.supplierList = suppliers || []
        this.warehouseList = warehouses || []
        this.productList = products || []
      } catch (e) { /* 忽略 */ }
    },
    async loadList() {
      this.loading = true
      try {
        const data = await getPurchaseReturnPage(this.query)
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
      this.query = { page: 1, size: 10, orderNo: '', supplierId: undefined, warehouseId: undefined, auditStatus: undefined }
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
    getSupplierName(id) {
      const s = this.supplierList.find(i => i.id === id)
      return s ? s.supplierName : '-'
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
      this.$nextTick(() => this.$refs.orderForm && this.$refs.orderForm.clearValidate())
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
      this.$nextTick(() => this.$refs.orderForm && this.$refs.orderForm.clearValidate())
    },
    async loadDetail(id) {
      try {
        const data = await getPurchaseReturn(id)
        this.form = {
          id: data.id,
          orderNo: data.orderNo || '',
          supplierId: data.supplierId,
          warehouseId: data.warehouseId,
          orderTime: data.orderTime,
          returnReason: data.returnReason || '',
          operatorId: data.operatorId,
          remark: data.remark || '',
          // 回显明细: 补充商品的大单位/换算率; 旧数据无录入数量时按基本单位还原
          details: (data.details || []).map(d => {
            const p = this.productList.find(i => i.id === d.productId)
            return {
              ...d,
              bigUnit: (p && p.bigUnit) || '',
              conversionRate: (p && p.conversionRate) || undefined,
              inputQuantity: d.inputQuantity != null ? d.inputQuantity : d.quantity
            }
          })
        }
      } catch (e) { /* 忽略 */ }
    },
    handleDelete(row) {
      this.$confirm(`确认删除采购退货单「${row.orderNo}」吗?`, '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deletePurchaseReturn(row.id)
            this.$message.success('删除成功')
            this.loadList()
          } catch (e) { /* 忽略 */ }
        }).catch(() => {})
    },
    handleAudit(row) {
      this.$prompt('出库后单据不可修改,且将扣减库存,确认出库?', '提示', {
        type: 'warning',
        inputPlaceholder: '可填写出库备注(选填)',
        inputType: 'textarea'
      }).then(async ({ value }) => {
        try {
          await auditPurchaseReturn(row.id, { remark: value || '' })
          this.$message.success('出库成功')
          this.loadList()
        } catch (e) { /* 忽略 */ }
      }).catch(() => {})
    },
    addDetail() {
      this.form.details.push({
        productId: undefined,
        productCode: '',
        productName: '',
        spec: '',
        unit: '',
        bigUnit: '',
        conversionRate: undefined,
        inputQuantity: 1,
        quantity: 1,
        price: 0,
        amount: 0,
        remark: ''
      })
    },
    removeDetail(idx) {
      this.form.details.splice(idx, 1)
    },
    /** 可选单位: 采购退货按大单位退(商品配置了大单位时),未配置则用基本单位 */
    unitOptions(row) {
      if (row.bigUnit) return [row.bigUnit]
      return [row.unit].filter(Boolean)
    },
    /** 切换单位: 按换算率换算默认单价与数量 */
    handleUnitChange(row) {
      const rate = Number(row.conversionRate) || 1
      const isBig = row.bigUnit && row.unit === row.bigUnit
      const basePrice = this.productList.find(i => i.id === row.productId)?.costPrice || 0
      if (isBig) {
        row.price = Number((basePrice * rate).toFixed(2))
        row.inputQuantity = Math.max(1, Math.floor((Number(row.inputQuantity) || 1) / rate) || 1)
      } else {
        row.price = basePrice
      }
      this.calcRow(row)
    },
    handleProductChange(row, val) {
      const p = this.productList.find(i => i.id === val)
      if (p) {
        row.productCode = p.productCode || ''
        row.productName = p.productName || ''
        row.spec = p.spec || ''
        row.bigUnit = p.bigUnit || ''
        row.conversionRate = p.conversionRate || undefined
        // 采购退货默认按大单位退; 未配置大单位时用基本单位
        if (p.bigUnit) {
          row.unit = p.bigUnit
          row.price = Number(((p.costPrice || 0) * (p.conversionRate || 1)).toFixed(2))
        } else {
          row.unit = p.unit || ''
          row.price = p.costPrice || 0
        }
      }
      this.calcRow(row)
    },
    /**
     * 行计算: 金额=录入数量×单价; 库存数量=录入数量×换算率(基本单位)
     */
    calcRow(row) {
      const rate = (row.bigUnit && row.unit === row.bigUnit) ? (Number(row.conversionRate) || 1) : 1
      const inputQty = Number(row.inputQuantity) || 0
      row.quantity = inputQty * rate
      row.amount = Number(((Number(row.price) || 0) * inputQty).toFixed(2))
    },
    handleSubmit() {
      this.$refs.orderForm.validate(async valid => {
        if (!valid) return
        if (!this.form.details || this.form.details.length === 0) {
          this.$message.warning('请至少添加一条退货明细')
          return
        }
        for (const d of this.form.details) {
          if (!d.productId) { this.$message.warning('请选择商品'); return }
          if (!d.inputQuantity || d.inputQuantity <= 0) { this.$message.warning('数量必须大于 0'); return }
          if (!d.quantity || d.quantity <= 0) { this.$message.warning('换算后数量必须大于 0'); return }
        }
        // 合计数量为换算后的基本单位数量
        this.form.totalQuantity = this.form.details.reduce((s, r) => s + (Number(r.quantity) || 0), 0)
        this.form.totalAmount = Number(this.form.details.reduce((s, r) => s + (Number(r.amount) || 0), 0).toFixed(2))
        this.submitting = true
        try {
          if (this.dialogType === 'add') {
            await addPurchaseReturn(this.form)
            this.$message.success('新增成功')
          } else {
            await updatePurchaseReturn({ ...this.form })
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
      this.$nextTick(() => this.$refs.orderForm && this.$refs.orderForm.clearValidate())
    },
    /** 导出 Excel: 两张 sheet, 采购退货单汇总 + 退货明细 */
    async handleExport() {
      if (this.exporting) return
      this.exporting = true
      try {
        const params = {
          orderNo: this.query.orderNo,
          supplierId: this.query.supplierId,
          warehouseId: this.query.warehouseId,
          auditStatus: this.query.auditStatus
        }
        const orders = await exportPurchaseReturns(params)
        if (!orders || orders.length === 0) {
          this.$message.warning('暂无数据可导出')
          return
        }

        // ===== Sheet1: 采购退货单汇总 =====
        const headerTitle = [['采购退货单汇总']]
        const headerCols = ['序号', '退货单号', '供应商', '仓库', '退货日期', '商品明细', '退货原因', '总数量', '总金额', '出库状态', '出库时间', '备注']
        const headerData = orders.map((o, i) => [
          i + 1,
          o.orderNo,
          this.getSupplierName(o.supplierId),
          this.getWarehouseName(o.warehouseId),
          o.orderTime || '',
          (o.details || []).map(d => `${d.productName || '-'}×${d.inputQuantity || d.quantity || 0}${d.unit || ''}`).join(', '),
          o.returnReason || '',
          o.totalQuantity != null ? o.totalQuantity : 0,
          o.totalAmount != null ? Number(o.totalAmount).toFixed(2) : '0.00',
          o.auditStatus === 1 ? '已出库' : '待出库',
          o.auditTime || '',
          o.remark || ''
        ])
        const headerRows = [...headerTitle, headerCols, ...headerData]

        // ===== Sheet2: 采购退货明细 =====
        const detailTitle = [['采购退货明细']]
        const detailCols = ['序号', '退货单号', '供应商', '仓库', '商品编码', '商品名称', '规格', '单位', '数量', '基本单位数量', '单价', '金额', '备注']
        const detailRows = []
        let idx = 1
        orders.forEach(o => {
          const details = o.details || []
          if (details.length === 0) {
            // 无明细也保留一行主表信息
            detailRows.push([
              idx++, o.orderNo, this.getSupplierName(o.supplierId), this.getWarehouseName(o.warehouseId),
              '', '', '', '', '', '', '', '', ''
            ])
          } else {
            details.forEach(d => {
              detailRows.push([
                idx++, o.orderNo, this.getSupplierName(o.supplierId), this.getWarehouseName(o.warehouseId),
                d.productCode || '', d.productName || '', d.spec || '', d.unit || '',
                d.inputQuantity != null ? d.inputQuantity : (d.quantity != null ? d.quantity : 0),
                d.quantity != null ? d.quantity : 0,
                d.price != null ? Number(d.price).toFixed(2) : '0.00',
                d.amount != null ? Number(d.amount).toFixed(2) : '0.00',
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

        wsHeader['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: headerCols.length - 1 } }]
        wsDetail['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: detailCols.length - 1 } }]

        wsHeader['!cols'] = [
          { wch: 6 }, { wch: 22 }, { wch: 18 }, { wch: 12 }, { wch: 20 },
          { wch: 40 }, { wch: 20 }, { wch: 10 }, { wch: 12 }, { wch: 10 }, { wch: 20 }, { wch: 20 }
        ]
        wsDetail['!cols'] = [
          { wch: 6 }, { wch: 22 }, { wch: 18 }, { wch: 12 }, { wch: 14 },
          { wch: 18 }, { wch: 12 }, { wch: 8 }, { wch: 10 }, { wch: 12 }, { wch: 10 }, { wch: 12 }, { wch: 20 }
        ]

        XLSX.utils.book_append_sheet(wb, wsHeader, '采购退货单汇总')
        XLSX.utils.book_append_sheet(wb, wsDetail, '采购退货明细')

        const ts = this.nowDateString()
        XLSX.writeFile(wb, `采购退货单_${ts}.xlsx`)
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
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  .search-bar { margin-bottom: 12px; }
  .pagination { margin-top: 12px; text-align: right; }
}
.detail-toolbar {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.detail-summary {
  display: flex;
  align-items: center;
  gap: 24px;
  font-size: 14px;
  color: #606266;
  .summary-item b { color: #303133; margin-left: 4px; }
  .summary-amount .amount {
    color: #f56c6c;
    font-size: 18px;
    margin-left: 4px;
  }
}
.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  .product-tag { margin: 0; }
}
.empty-tip { color: #c0c4cc; }
</style>
