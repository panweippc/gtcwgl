<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :inline="true" :model="query" size="small" class="search-form">
      <el-form-item label="单据编号">
        <el-input v-model="query.orderNo" placeholder="请输入盘点单编号" clearable @keyup.enter.native="handleSearch" />
      </el-form-item>
      <el-form-item label="仓库">
        <el-select v-model="query.warehouseId" placeholder="全部" clearable filterable style="width:160px">
          <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="盘点状态">
        <el-select v-model="query.auditStatus" placeholder="全部" clearable style="width:120px">
          <el-option label="待盘点" :value="0" />
          <el-option label="已盘点" :value="1" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 列表 -->
    <div class="table-toolbar">
      <el-button type="success" icon="el-icon-plus" @click="handleAdd">新增盘点</el-button>
      <el-button type="warning" icon="el-icon-download" :loading="exporting" @click="handleExport">导出Excel</el-button>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="orderNo" label="盘点单编号" min-width="170" />
      <el-table-column label="仓库" min-width="120">
        <template slot-scope="{ row }">{{ getWarehouseName(row.warehouseId) }}</template>
      </el-table-column>
      <el-table-column label="盘点商品" min-width="240">
        <template slot-scope="{ row }">
          <div v-if="row.details && row.details.length" class="product-tags">
            <el-tag v-for="(d, idx) in row.details" :key="idx" size="mini" effect="plain" class="product-tag">
              {{ d.productName || '-' }} (账{{ d.bookQuantity != null ? d.bookQuantity : 0 }}<template v-if="d.actualQuantity != null">/实{{ d.actualQuantity }}</template>)
            </el-tag>
          </div>
          <span v-else class="empty-tip">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="totalBookQuantity" label="账面总数" width="90" align="right" />
      <el-table-column prop="totalActualQuantity" label="实盘总数" width="90" align="right" />
      <el-table-column label="盘盈数量" width="90" align="right">
        <template slot-scope="{ row }">
          <span :class="{ 'profit-num': row.totalProfitQuantity > 0 }">{{ row.totalProfitQuantity || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="盘亏数量" width="90" align="right">
        <template slot-scope="{ row }">
          <span :class="{ 'loss-num': row.totalLossQuantity > 0 }">{{ row.totalLossQuantity || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="orderTime" label="单据日期" min-width="160" />
      <el-table-column label="盘点状态" width="100" align="center">
        <template slot-scope="{ row }">
          <el-tag :type="row.auditStatus === 1 ? 'success' : 'warning'" size="small">
            {{ row.auditStatus === 1 ? '已盘点' : '待盘点' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="盘盈/盘亏单号" min-width="200">
        <template slot-scope="{ row }">
          <div v-if="row.profitOrderNo" class="order-no-line">盈：{{ row.profitOrderNo }}</div>
          <div v-if="row.lossOrderNo" class="order-no-line">亏：{{ row.lossOrderNo }}</div>
          <span v-if="!row.profitOrderNo && !row.lossOrderNo" class="empty-tip">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center">
        <template slot-scope="{ row }">
          <el-button type="text" icon="el-icon-view" @click="handleView(row)">查看</el-button>
          <el-button type="text" icon="el-icon-printer" @click="handlePrint(row)">打印</el-button>
          <template v-if="row.auditStatus !== 1">
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(row)">删除</el-button>
            <el-button type="text" icon="el-icon-s-check" style="color:#67C23A" @click="handleAudit(row)">盘点</el-button>
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
      width="85%"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form ref="checkForm" :model="form" :rules="rules" :disabled="formDisabled" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="盘点单编号">
              <el-input v-model="form.orderNo" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select
                v-model="form.warehouseId"
                placeholder="请选择仓库"
                filterable
                style="width:100%"
                :disabled="dialogType !== 'add'"
                @change="handleWarehouseChange"
              >
                <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
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
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="请输入备注" maxlength="200" show-word-limit />
        </el-form-item>

        <!-- 明细 -->
        <div class="detail-header">
          <span class="detail-title">盘点明细</span>
          <el-button
            v-if="!formDisabled"
            type="primary"
            plain
            size="small"
            icon="el-icon-refresh"
            :loading="bookLoading"
            @click="reloadBook"
          >重新带出库存</el-button>
        </div>
        <el-table v-loading="bookLoading" :data="form.details" border stripe size="small" max-height="420">
          <el-table-column type="index" label="序号" width="50" align="center" />
          <el-table-column prop="productCode" label="商品编码" width="130" />
          <el-table-column prop="productName" label="商品名称" min-width="180" />
          <el-table-column prop="spec" label="规格" width="120" />
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column label="账面数量" width="100" align="right">
            <template slot-scope="{ row }">{{ row.bookQuantity != null ? row.bookQuantity : 0 }}</template>
          </el-table-column>
          <el-table-column label="实盘数量" width="160">
            <template slot-scope="{ row }">
              <el-input-number
                v-model="row.actualQuantity"
                :min="0"
                :precision="0"
                :step="1"
                controls-position="right"
                size="small"
                placeholder="未填则忽略"
                style="width:100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="差异" width="100" align="right">
            <template slot-scope="{ row }">
              <span v-if="row.actualQuantity == null" class="empty-tip">-</span>
              <span v-else :class="diffClass(row)">{{ diffValue(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="160">
            <template slot-scope="{ row }">
              <el-input v-model="row.remark" placeholder="备注" size="small" />
            </template>
          </el-table-column>
        </el-table>
        <div class="detail-toolbar">
          <span class="empty-tip">提示：未录入实盘数量的行将被忽略，不参与差异计算</span>
          <div class="detail-summary">
            <span class="summary-item">账面合计: <b>{{ totalBook }}</b></span>
            <span class="summary-item">实盘合计: <b>{{ totalActual }}</b></span>
            <span class="summary-item">盘盈合计: <b class="profit-num">{{ totalProfit }}</b></span>
            <span class="summary-item">盘亏合计: <b class="loss-num">{{ totalLoss }}</b></span>
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
  getStockCheckPage,
  getStockCheck,
  getStockCheckBookList,
  addStockCheck,
  updateStockCheck,
  deleteStockCheck,
  auditStockCheck,
  exportStockChecks
} from '@/api/stock'
import { getAllWarehouses } from '@/api/warehouse'
import * as XLSX from 'xlsx'

export default {
  name: 'StockCheck',
  data() {
    return {
      loading: false,
      submitting: false,
      exporting: false,
      bookLoading: false,
      list: [],
      total: 0,
      query: { page: 1, size: 10, orderNo: '', warehouseId: undefined, auditStatus: undefined },
      warehouseList: [],
      dialogVisible: false,
      dialogType: 'add',
      form: this.getEmptyForm(),
      rules: {
        warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
        orderTime: [{ required: true, message: '请选择单据日期', trigger: 'change' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      const map = { add: '新增盘点单', edit: '编辑盘点单', view: '查看盘点单' }
      return map[this.dialogType] || '盘点单'
    },
    formDisabled() {
      return this.dialogType === 'view'
    },
    // 账面合计(仅统计已录入实盘数量的行)
    totalBook() {
      return (this.form.details || []).reduce((s, r) => {
        if (r.actualQuantity == null) return s
        return s + (Number(r.bookQuantity) || 0)
      }, 0)
    },
    // 实盘合计
    totalActual() {
      return (this.form.details || []).reduce((s, r) => {
        if (r.actualQuantity == null) return s
        return s + (Number(r.actualQuantity) || 0)
      }, 0)
    },
    // 盘盈合计
    totalProfit() {
      return (this.form.details || []).reduce((s, r) => {
        const d = this.diffValue(r)
        return s + (d != null && d > 0 ? d : 0)
      }, 0)
    },
    // 盘亏合计
    totalLoss() {
      return (this.form.details || []).reduce((s, r) => {
        const d = this.diffValue(r)
        return s + (d != null && d < 0 ? -d : 0)
      }, 0)
    }
  },
  created() {
    this.loadOptions()
    this.loadList()
  },
  activated() {
    // 页面被 keep-alive 缓存,再次激活时刷新基础数据(仓库可能在其他页面修改)
    this.loadOptions()
  },
  methods: {
    getEmptyForm() {
      return {
        id: undefined,
        orderNo: '',
        warehouseId: undefined,
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
        const warehouses = await getAllWarehouses()
        this.warehouseList = warehouses || []
      } catch (e) { /* 忽略 */ }
    },
    async loadList() {
      this.loading = true
      try {
        const data = await getStockCheckPage(this.query)
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
      this.query = { page: 1, size: 10, orderNo: '', warehouseId: undefined, auditStatus: undefined }
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
    /** 差异值: 实盘-账面, 未填实盘返回 null */
    diffValue(row) {
      if (row.actualQuantity == null) return null
      return (Number(row.actualQuantity) || 0) - (Number(row.bookQuantity) || 0)
    },
    /** 差异样式: 盘盈绿、盘亏红 */
    diffClass(row) {
      const d = this.diffValue(row)
      if (d == null) return ''
      if (d > 0) return 'profit-num'
      if (d < 0) return 'loss-num'
      return ''
    },
    handleAdd() {
      this.dialogType = 'add'
      this.form = this.getEmptyForm()
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.checkForm && this.$refs.checkForm.clearValidate())
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
      this.$nextTick(() => this.$refs.checkForm && this.$refs.checkForm.clearValidate())
    },
    async loadDetail(id) {
      try {
        const data = await getStockCheck(id)
        this.form = {
          id: data.id,
          orderNo: data.orderNo || '',
          warehouseId: data.warehouseId,
          orderTime: data.orderTime,
          remark: data.remark || '',
          details: (data.details || []).map(d => ({ ...d }))
        }
      } catch (e) { /* 忽略 */ }
    },
    /** 选择仓库后自动带出账面库存 */
    async handleWarehouseChange(warehouseId) {
      if (!warehouseId) {
        this.form.details = []
        return
      }
      this.form.details = []
      await this.reloadBook(true)
    },
    /** 重新带出库存(保留已录入的实盘数量与备注) */
    async reloadBook(force) {
      if (!this.form.warehouseId) {
        this.$message.warning('请先选择仓库')
        return
      }
      this.bookLoading = true
      try {
        const books = await getStockCheckBookList(this.form.warehouseId)
        const oldMap = {}
        ;(this.form.details || []).forEach(d => { if (d.productId != null) oldMap[d.productId] = d })
        this.form.details = (books || []).map(b => {
          const old = oldMap[b.productId]
          return {
            productId: b.productId,
            productCode: b.productCode,
            productName: b.productName,
            spec: b.spec,
            unit: b.unit,
            bookQuantity: b.bookQuantity != null ? b.bookQuantity : 0,
            actualQuantity: old ? old.actualQuantity : undefined,
            remark: old ? old.remark : ''
          }
        })
        if (!this.form.details.length) {
          this.$message.warning('该仓库暂无账面库存数据')
        } else if (force) {
          this.$message.success(`已带出 ${this.form.details.length} 条账面库存`)
        } else {
          this.$message.success(`已重新带出 ${this.form.details.length} 条账面库存`)
        }
      } catch (e) { /* 忽略 */ } finally {
        this.bookLoading = false
      }
    },
    handleDelete(row) {
      this.$confirm(`确认删除盘点单「${row.orderNo}」吗?`, '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deleteStockCheck(row.id)
            this.$message.success('删除成功')
            this.loadList()
          } catch (e) { /* 忽略 */ }
        }).catch(() => {})
    },
    handleAudit(row) {
      this.$prompt('盘点后将生成盘盈/盘亏单，盘盈增加库存、盘亏扣减库存，且单据不可再修改，确认盘点?', '提示', {
        type: 'warning',
        inputPlaceholder: '可填写盘点备注(选填)',
        inputType: 'textarea'
      }).then(async ({ value }) => {
        try {
          await auditStockCheck(row.id, { remark: value || '' })
          this.$message.success('盘点成功')
          this.loadList()
        } catch (e) { /* 忽略 */ }
      }).catch(() => {})
    },
    handleSubmit() {
      this.$refs.checkForm.validate(async valid => {
        if (!valid) return
        const details = this.form.details || []
        if (!details.length) {
          this.$message.warning('请先带出账面库存')
          return
        }
        if (!details.some(d => d.actualQuantity != null)) {
          this.$message.warning('请至少录入一条实盘数量')
          return
        }
        if (details.some(d => d.actualQuantity != null && d.actualQuantity < 0)) {
          this.$message.warning('实盘数量不能为负数')
          return
        }
        this.submitting = true
        try {
          if (this.dialogType === 'add') {
            await addStockCheck(this.form)
            this.$message.success('新增成功')
          } else {
            await updateStockCheck({ ...this.form })
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
      this.$nextTick(() => this.$refs.checkForm && this.$refs.checkForm.clearValidate())
    },
    /** 导出 Excel: 两张 sheet, 盘点单汇总 + 盘点明细 */
    async handleExport() {
      if (this.exporting) return
      this.exporting = true
      try {
        const params = {
          orderNo: this.query.orderNo,
          warehouseId: this.query.warehouseId,
          auditStatus: this.query.auditStatus
        }
        const orders = await exportStockChecks(params)
        if (!orders || orders.length === 0) {
          this.$message.warning('暂无数据可导出')
          return
        }

        // ===== Sheet1: 盘点单汇总 =====
        const headerTitle = [['库存盘点单汇总']]
        const headerCols = ['序号', '盘点单编号', '仓库', '单据日期', '商品种类', '账面总数', '实盘总数', '盘盈数量', '盘亏数量', '盘点状态', '盘盈单号', '盘亏单号', '备注']
        const headerData = orders.map((o, i) => [
          i + 1,
          o.orderNo,
          this.getWarehouseName(o.warehouseId),
          o.orderTime || '',
          (o.details || []).length,
          o.totalBookQuantity != null ? o.totalBookQuantity : 0,
          o.totalActualQuantity != null ? o.totalActualQuantity : 0,
          o.totalProfitQuantity != null ? o.totalProfitQuantity : 0,
          o.totalLossQuantity != null ? o.totalLossQuantity : 0,
          o.auditStatus === 1 ? '已盘点' : '待盘点',
          o.profitOrderNo || '',
          o.lossOrderNo || '',
          o.remark || ''
        ])
        const headerRows = [...headerTitle, headerCols, ...headerData]

        // ===== Sheet2: 盘点明细 =====
        const detailTitle = [['库存盘点明细']]
        const detailCols = ['序号', '盘点单编号', '仓库', '商品编码', '商品名称', '规格', '单位', '账面数量', '实盘数量', '差异数量', '备注']
        const detailRows = []
        let idx = 1
        orders.forEach(o => {
          const details = o.details || []
          if (details.length === 0) {
            detailRows.push([idx++, o.orderNo, this.getWarehouseName(o.warehouseId), '', '', '', '', '', '', '', ''])
          } else {
            details.forEach(d => {
              const actual = d.actualQuantity != null ? d.actualQuantity : ''
              const diff = d.actualQuantity != null ? (Number(d.actualQuantity) - (Number(d.bookQuantity) || 0)) : ''
              detailRows.push([
                idx++, o.orderNo, this.getWarehouseName(o.warehouseId),
                d.productCode || '', d.productName || '', d.spec || '', d.unit || '',
                d.bookQuantity != null ? d.bookQuantity : 0, actual, diff, d.remark || ''
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
          { wch: 6 }, { wch: 24 }, { wch: 14 }, { wch: 20 }, { wch: 10 },
          { wch: 10 }, { wch: 10 }, { wch: 10 }, { wch: 10 }, { wch: 10 }, { wch: 24 }, { wch: 24 }, { wch: 20 }
        ]
        wsDetail['!cols'] = [
          { wch: 6 }, { wch: 24 }, { wch: 14 }, { wch: 14 },
          { wch: 20 }, { wch: 12 }, { wch: 8 }, { wch: 10 }, { wch: 10 }, { wch: 10 }, { wch: 20 }
        ]

        XLSX.utils.book_append_sheet(wb, wsHeader, '盘点单汇总')
        XLSX.utils.book_append_sheet(wb, wsDetail, '盘点明细')

        const ts = this.nowDateString()
        XLSX.writeFile(wb, `库存盘点单_${ts}.xlsx`)
        this.$message.success(`导出成功,共 ${orders.length} 张单据`)
      } catch (e) {
        console.error(e)
        this.$message.error('导出失败: ' + (e.message || '未知错误'))
      } finally {
        this.exporting = false
      }
    },
    /** 打印单张盘点单 */
    async handlePrint(row) {
      try {
        const data = await getStockCheck(row.id)
        if (!data) {
          this.$message.warning('盘点单不存在')
          return
        }
        const details = data.details || []
        const rows = details.map((d, i) => `
          <tr>
            <td style="text-align:center">${i + 1}</td>
            <td>${d.productCode || ''}</td>
            <td>${d.productName || ''}</td>
            <td>${d.spec || ''}</td>
            <td style="text-align:center">${d.unit || ''}</td>
            <td style="text-align:right">${d.bookQuantity != null ? d.bookQuantity : 0}</td>
            <td style="text-align:right">${d.actualQuantity != null ? d.actualQuantity : ''}</td>
            <td style="text-align:right;${d.difference > 0 ? 'color:#67C23A' : d.difference < 0 ? 'color:#F56C6C' : ''}">
              ${d.actualQuantity != null ? (d.difference || 0) : ''}
            </td>
            <td>${d.remark || ''}</td>
          </tr>`).join('')

        const html = `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>库存盘点单 ${data.orderNo || ''}</title>
  <style>
    body { font-family: "Microsoft YaHei", Arial, sans-serif; color: #333; padding: 16px; }
    h1 { text-align: center; font-size: 22px; margin: 0 0 16px; }
    .info { display: flex; flex-wrap: wrap; margin-bottom: 12px; font-size: 13px; }
    .info div { width: 33.33%; line-height: 24px; }
    .info span { color: #888; }
    table { width: 100%; border-collapse: collapse; font-size: 12px; }
    th, td { border: 1px solid #999; padding: 6px 8px; }
    th { background: #f2f2f2; }
    .foot { margin-top: 24px; display: flex; justify-content: space-between; font-size: 13px; }
    @media print { body { padding: 0; } }
  </style>
</head>
<body>
  <h1>库存盘点单</h1>
  <div class="info">
    <div><span>盘点单编号：</span>${data.orderNo || ''}</div>
    <div><span>仓库：</span>${this.getWarehouseName(data.warehouseId)}</div>
    <div><span>单据日期：</span>${data.orderTime || ''}</div>
    <div><span>盘点状态：</span>${data.auditStatus === 1 ? '已盘点' : '待盘点'}</div>
    <div><span>盘盈单号：</span>${data.profitOrderNo || '-'}</div>
    <div><span>盘亏单号：</span>${data.lossOrderNo || '-'}</div>
    <div style="width:100%"><span>备注：</span>${data.remark || '-'}</div>
  </div>
  <table>
    <thead>
      <tr>
        <th style="width:40px">序号</th>
        <th style="width:110px">商品编码</th>
        <th>商品名称</th>
        <th style="width:90px">规格</th>
        <th style="width:50px">单位</th>
        <th style="width:70px">账面数量</th>
        <th style="width:70px">实盘数量</th>
        <th style="width:70px">差异</th>
        <th style="width:110px">备注</th>
      </tr>
    </thead>
    <tbody>${rows || '<tr><td colspan="9" style="text-align:center">暂无明细</td></tr>'}</tbody>
  </table>
  <div class="foot">
    <div>账面合计：${data.totalBookQuantity != null ? data.totalBookQuantity : 0}</div>
    <div>实盘合计：${data.totalActualQuantity != null ? data.totalActualQuantity : 0}</div>
    <div>盘盈合计：${data.totalProfitQuantity != null ? data.totalProfitQuantity : 0}</div>
    <div>盘亏合计：${data.totalLossQuantity != null ? data.totalLossQuantity : 0}</div>
  </div>
</body>
</html>`

        // 隐藏 iframe(非零尺寸,浏览器允许打印),用 onload 事件确保内容渲染后再唤起打印
        const iframe = document.createElement('iframe')
        iframe.setAttribute('style', 'position:fixed;left:-9999px;top:0;width:1024px;height:768px;border:0;')
        // 打印完成后清理 iframe
        const clearIframe = () => {
          if (iframe.parentNode) {
            try { iframe.parentNode.removeChild(iframe) } catch (e) { /* 忽略 */ }
          }
        }
        // 1) 先把 iframe 挂到 DOM, 这一步之后 contentWindow 才可用
        document.body.appendChild(iframe)
        // 2) 挂到 DOM 后再取 contentWindow/document 并写入 HTML
        const cw = iframe.contentWindow
        const doc = cw.document
        doc.open()
        doc.write(html)
        doc.close()
        // 3) onload 触发时内容已渲染完毕, 唤起打印
        iframe.onload = () => {
          try {
            cw.focus()
            cw.print()
          } catch (e) { /* 忽略 */ }
          // 打印对话框关闭后清理, 兜底 3 秒
          setTimeout(clearIframe, 3000)
        }
      } catch (e) {
        this.$message.error('打印失败: ' + (e.message || '未知错误'))
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
  .pagination { margin-top: 12px; text-align: right; }
}
/* 覆盖全局 search-form 最后一个 el-form-item 推右样式,使盘点状态与查询按钮紧跟仓库 */
::v-deep .search-form .el-form-item:last-child {
  margin-left: 0 !important;
}
::v-deep .search-form .el-form-item .el-select + .el-button {
  margin-left: 10px;
}
.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  .product-tag { margin: 0; }
}
.empty-tip { color: #c0c4cc; font-size: 12px; }
/* 盘盈绿色、盘亏红色 */
.profit-num { color: #67C23A; font-weight: 600; }
.loss-num { color: #F56C6C; font-weight: 600; }
.order-no-line { font-size: 12px; line-height: 18px; color: #606266; }
/* 明细头部:左侧标题,右侧带出按钮 */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0 0 8px 100px;
  .detail-title { font-size: 14px; font-weight: 600; color: #303133; }
}
/* 明细工具栏:左侧提示,右侧合计 */
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
  }
}
</style>
