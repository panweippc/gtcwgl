<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :inline="true" :model="query" size="small" class="search-bar search-form">
      <el-form-item label="单据编号">
        <el-input v-model="query.orderNo" placeholder="请输入单据编号" clearable @keyup.enter.native="handleSearch" style="width:180px" />
      </el-form-item>
      <el-form-item label="客户">
        <el-select v-model="query.customerId" placeholder="全部" clearable filterable style="width:180px">
          <el-option v-for="item in customerList" :key="item.id" :label="item.customerName" :value="item.id" />
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
      <el-table-column prop="orderNo" label="单据编号" min-width="160" />
      <el-table-column label="客户" min-width="160">
        <template slot-scope="{ row }">{{ getCustomerName(row.customerId) }}</template>
      </el-table-column>
      <el-table-column label="仓库" min-width="120">
        <template slot-scope="{ row }">{{ getWarehouseName(row.warehouseId) }}</template>
      </el-table-column>
      <el-table-column label="销售商品" min-width="260">
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
      <el-table-column prop="orderTime" label="单据日期" min-width="160" />
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
            <el-form-item label="单据编号">
              <el-input v-model="form.orderNo" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" filterable style="width:100%">
                <el-option v-for="item in customerList" :key="item.id" :label="item.customerName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" placeholder="请选择仓库" filterable style="width:100%">
                <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="单据日期" prop="orderTime">
              <el-date-picker
                v-model="form.orderTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="选择单据日期"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="请输入备注" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

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
              <el-input-number v-model="row.price" :min="0" :precision="2" :step="0.01" controls-position="right" size="small" @change="(val) => handlePriceChange(row, val)" />
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
        <div class="detail-summary">
          <el-button type="primary" plain icon="el-icon-plus" size="small" :disabled="formDisabled" @click="addDetail">添加明细</el-button>
          <div style="flex:1"></div>
          <span class="summary-item">商品种类<b>{{ detailCount }}</b></span>
          <span class="summary-item">合计数量<b>{{ totalQuantity }}</b></span>
          <span class="summary-amount">合计金额<b class="amount">{{ formatMoney(totalAmount) }}</b></span>
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
  getSaleOrderPage,
  getSaleOrder,
  addSaleOrder,
  updateSaleOrder,
  deleteSaleOrder,
  auditSaleOrder,
  exportSaleOrders
} from '@/api/sale'
import { getAllProducts } from '@/api/product'
import { getAllWarehouses } from '@/api/warehouse'
import { getAllCustomers } from '@/api/customer'
import { formatMoney } from '@/utils/format'
import * as XLSX from 'xlsx'

export default {
  name: 'SaleOrder',
  data() {
    return {
      loading: false,
      submitting: false,
      exporting: false,
      list: [],
      total: 0,
      query: { page: 1, size: 10, orderNo: '', customerId: undefined, warehouseId: undefined, auditStatus: undefined },
      customerList: [],
      warehouseList: [],
      productList: [],
      dialogVisible: false,
      dialogType: 'add',
      form: this.getEmptyForm(),
      rules: {
        customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
        warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
        orderTime: [{ required: true, message: '请选择单据日期', trigger: 'change' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      const map = { add: '新增销售单', edit: '编辑销售单', view: '查看销售单' }
      return map[this.dialogType] || '销售单'
    },
    formDisabled() {
      return this.dialogType === 'view'
    },
    detailCount() {
      const ids = new Set((this.form.details || []).map(d => d.productId).filter(Boolean))
      return ids.size
    },
    totalQuantity() {
      return (this.form.details || []).reduce((s, r) => s + (Number(r.quantity) || 0), 0)
    },
    totalAmount() {
      return Number(((this.form.details || []).reduce((s, r) => s + (Number(r.amount) || 0), 0)).toFixed(2))
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
        customerId: undefined,
        warehouseId: undefined,
        orderTime: this.nowDateTime(),
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
        const [customers, warehouses, products] = await Promise.all([
          getAllCustomers(),
          getAllWarehouses(),
          getAllProducts()
        ])
        this.customerList = customers || []
        this.warehouseList = warehouses || []
        this.productList = products || []
      } catch (e) { /* 忽略 */ }
    },
    async loadList() {
      this.loading = true
      try {
        const data = await getSaleOrderPage(this.query)
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
      this.query = { page: 1, size: 10, orderNo: '', customerId: undefined, warehouseId: undefined, auditStatus: undefined }
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
    getCustomerName(id) {
      const c = this.customerList.find(i => i.id === id)
      return c ? c.customerName : '-'
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
        const data = await getSaleOrder(id)
        this.form = {
          id: data.id,
          orderNo: data.orderNo || '',
          customerId: data.customerId,
          warehouseId: data.warehouseId,
          orderTime: data.orderTime,
          operatorId: data.operatorId,
          remark: data.remark || '',
          details: (data.details || []).map(d => {
            // 补全预设售价/大单位/换算率; 旧数据无录入数量时按基本单位还原
            const p = this.productList.find(i => i.id === d.productId)
            return {
              ...d,
              salePrice: p ? p.salePrice : d.price,
              baseUnit: (p && p.unit) || d.unit || '',  // 基本单位(固定,供 unitOptions 使用)
              bigUnit: (p && p.bigUnit) || '',
              conversionRate: (p && p.conversionRate) || undefined,
              inputQuantity: d.inputQuantity != null ? d.inputQuantity : d.quantity
            }
          })
        }
      } catch (e) { /* 忽略 */ }
    },
    handleDelete(row) {
      this.$confirm(`确认删除销售单「${row.orderNo}」吗?`, '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deleteSaleOrder(row.id)
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
          await auditSaleOrder(row.id, { remark: value || '' })
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
        baseUnit: '',   // 基本单位(固定不变,供 unitOptions 使用)
        bigUnit: '',
        conversionRate: undefined,
        inputQuantity: 1,
        quantity: 1,
        price: 0,
        salePrice: 0,  // 预设售价(基本单位),用于低价校验
        amount: 0,
        remark: ''
      })
    },
    removeDetail(idx) {
      this.form.details.splice(idx, 1)
    },
    /** 可选单位: 基本单位 + 大单位(商品配置了才显示) */
    unitOptions(row) {
      const list = []
      if (row.baseUnit) list.push(row.baseUnit)
      if (row.bigUnit && row.bigUnit !== row.baseUnit) list.push(row.bigUnit)
      return list
    },
    /** 切换单位: 按换算率换算默认单价与数量 */
    handleUnitChange(row) {
      const rate = Number(row.conversionRate) || 1
      const isBig = row.bigUnit && row.unit === row.bigUnit
      const basePrice = this.productList.find(i => i.id === row.productId)?.salePrice || 0
      if (isBig) {
        row.price = Number((basePrice * rate).toFixed(2))
        row.inputQuantity = Math.max(1, Math.floor((Number(row.inputQuantity) || 1) / rate) || 1)
      } else {
        row.price = basePrice
      }
      this.calcRow(row)
      // 切换单位后重新做低价校验
      this.handlePriceChange(row, row.price)
    },
    handleProductChange(row, val) {
      const p = this.productList.find(i => i.id === val)
      if (p) {
        row.productCode = p.productCode || ''
        row.productName = p.productName || ''
        row.spec = p.spec || ''
        row.unit = p.unit || ''
        row.baseUnit = p.unit || ''
        row.bigUnit = p.bigUnit || ''
        row.conversionRate = p.conversionRate || undefined
        row.price = p.salePrice || 0
        row.salePrice = p.salePrice || 0  // 保存预设售价(基本单位),用于低价校验
      }
      this.calcRow(row)
    },
    /** 售价修改回调:折算到基本单位后低于预设售价时弹窗提醒 */
    handlePriceChange(row, val) {
      this.calcRow(row)
      const standard = Number(row.salePrice) || 0
      const current = Number(val) || 0
      // 大单位单价折算为基本单位单价再比较
      const rate = (row.bigUnit && row.unit === row.bigUnit) ? (Number(row.conversionRate) || 1) : 1
      const currentBase = current / rate
      if (standard > 0 && currentBase > 0 && currentBase < standard) {
        const diff = (standard - currentBase).toFixed(2)
        const percent = ((standard - currentBase) / standard * 100).toFixed(1)
        this.$message.warning({
          message: `【低价提醒】${row.productName || ''} 预设售价 ¥${standard.toFixed(2)}/${row.unit || ''},当前折算 ¥${currentBase.toFixed(2)}/${row.unit || ''},低于预设 ¥${diff} (${percent}%),请确认是否亏损出货`,
          duration: 5000,
          showClose: true
        })
      }
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
          this.$message.warning('请至少添加一条明细')
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
            await addSaleOrder(this.form)
            this.$message.success('新增成功')
          } else {
            await updateSaleOrder({ ...this.form })
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
    /** 导出 Excel: 两张 sheet, 销售出库单汇总 + 销售明细 */
    async handleExport() {
      if (this.exporting) return
      this.exporting = true
      try {
        const params = {
          orderNo: this.query.orderNo,
          customerId: this.query.customerId,
          warehouseId: this.query.warehouseId,
          auditStatus: this.query.auditStatus
        }
        const orders = await exportSaleOrders(params)
        if (!orders || orders.length === 0) {
          this.$message.warning('暂无数据可导出')
          return
        }

        // ===== Sheet1: 销售出库单汇总 =====
        const headerTitle = [['销售出库单汇总']]
        const headerCols = ['序号', '单据编号', '客户', '仓库', '单据日期', '商品明细', '总数量', '总金额', '出库状态', '出库时间', '备注']
        const headerData = orders.map((o, i) => [
          i + 1,
          o.orderNo,
          this.getCustomerName(o.customerId),
          this.getWarehouseName(o.warehouseId),
          o.orderTime || '',
          (o.details || []).map(d => `${d.productName || '-'}×${d.inputQuantity || d.quantity || 0}${d.unit || ''}`).join(', '),
          o.totalQuantity != null ? o.totalQuantity : 0,
          o.totalAmount != null ? Number(o.totalAmount).toFixed(2) : '0.00',
          o.auditStatus === 1 ? '已出库' : '待出库',
          o.auditTime || '',
          o.remark || ''
        ])
        const headerRows = [...headerTitle, headerCols, ...headerData]

        // ===== Sheet2: 销售明细 =====
        const detailTitle = [['销售出库明细']]
        const detailCols = ['序号', '单据编号', '客户', '仓库', '商品编码', '商品名称', '规格', '单位', '数量', '基本单位数量', '单价', '金额', '备注']
        const detailRows = []
        let idx = 1
        orders.forEach(o => {
          const details = o.details || []
          if (details.length === 0) {
            detailRows.push([
              idx++, o.orderNo, this.getCustomerName(o.customerId), this.getWarehouseName(o.warehouseId),
              '', '', '', '', '', '', '', '', ''
            ])
          } else {
            details.forEach(d => {
              detailRows.push([
                idx++, o.orderNo, this.getCustomerName(o.customerId), this.getWarehouseName(o.warehouseId),
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
          { wch: 40 }, { wch: 10 }, { wch: 12 }, { wch: 10 }, { wch: 20 }, { wch: 20 }
        ]
        wsDetail['!cols'] = [
          { wch: 6 }, { wch: 22 }, { wch: 18 }, { wch: 12 }, { wch: 14 },
          { wch: 18 }, { wch: 12 }, { wch: 8 }, { wch: 10 }, { wch: 10 }, { wch: 12 }, { wch: 20 }
        ]

        XLSX.utils.book_append_sheet(wb, wsHeader, '销售出库单汇总')
        XLSX.utils.book_append_sheet(wb, wsDetail, '销售出库明细')

        const ts = this.nowDateString()
        XLSX.writeFile(wb, `销售出库单_${ts}.xlsx`)
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
.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  .product-tag { margin: 0; }
}
.empty-tip { color: #c0c4cc; }
.detail-summary {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
  .summary-item b { color: #303133; margin-left: 4px; }
  .summary-amount .amount {
    color: #f56c6c;
    font-size: 18px;
    margin-left: 4px;
  }
}
</style>
