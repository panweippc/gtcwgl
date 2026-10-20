<template>
  <div class="app-container">
    <!-- 顶部查询 -->
    <el-form :inline="true" size="small" class="search-bar">
      <el-form-item label="起止日期">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="yyyy-MM-dd"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :picker-options="pickerOptions"
          @change="handleSearch"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh-left" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 汇总统计卡片 -->
    <el-row :gutter="16" v-loading="loading" class="summary-row">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-inner">
            <i :class="card.icon" :style="{ color: card.color, fontSize: '40px' }"></i>
            <div class="summary-text">
              <div class="summary-value">{{ card.value }}</div>
              <div class="summary-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 采购/采购退货趋势图(一行并排) -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never">
          <div slot="header" class="chart-header">
            <span>采购趋势</span>
            <i class="el-icon-zoom-in chart-zoom" title="放大查看" @click="openLargeChart('purchase')"></i>
          </div>
          <div ref="chartEl" class="chart" v-loading="loading"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div slot="header" class="chart-header">
            <span>采购退货趋势</span>
            <i class="el-icon-zoom-in chart-zoom" title="放大查看" @click="openLargeChart('return')"></i>
          </div>
          <div ref="returnChartEl" class="chart" v-loading="loading"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表放大弹窗 -->
    <el-dialog
      :visible.sync="largeVisible"
      :title="largeTitle"
      width="86%"
      top="5vh"
      append-to-body
      @opened="renderLargeChart"
      @closed="disposeLargeChart"
    >
      <div ref="largeChartEl" class="large-chart"></div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getDailyPurchase, getPurchaseSummary, getDailyPurchaseReturn } from '@/api/stats'
import { formatMoney } from '@/utils/format'

/**
 * 获取默认日期区间(近30天,含今天)
 * @returns {[string, string]} [开始日期, 结束日期], 格式 yyyy-MM-dd
 */
function getDefaultRange() {
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() - 29 * 24 * 3600 * 1000)
  const pad = n => (n < 10 ? '0' + n : '' + n)
  return [
    `${start.getFullYear()}-${pad(start.getMonth() + 1)}-${pad(start.getDate())}`,
    `${end.getFullYear()}-${pad(end.getMonth() + 1)}-${pad(end.getDate())}`
  ]
}

export default {
  name: 'StatsPurchase',
  data() {
    return {
      loading: false,
      dateRange: getDefaultRange(),
      // 采购/退货汇总数据
      summary: {},
      chart: null,
      // 采购退货趋势图实例
      returnChart: null,
      // 放大弹窗可见性
      largeVisible: false,
      // 放大弹窗标题
      largeTitle: '',
      // 当前放大的图表类型:purchase/return
      largeType: 'purchase',
      // 放大弹窗图表实例
      largeChart: null,
      // 采购/退货按日数据(放大弹窗复用)
      purchaseDaily: [],
      returnDaily: [],
      pickerOptions: {
        disabledDate(time) { return time.getTime() > Date.now() }
      }
    }
  },
  computed: {
    // 汇总卡片:采购总数量/采购总金额/退货总数量/退货总金额
    cards() {
      const s = this.summary || {}
      return [
        { label: '采购总数量', value: s.purchaseTotalQuantity || 0, icon: 'el-icon-shopping-bag-2', color: '#409EFF' },
        { label: '采购总金额(元)', value: formatMoney(s.purchaseTotalAmount), icon: 'el-icon-shopping-cart-full', color: '#67C23A' },
        { label: '退货总数量', value: s.returnTotalQuantity || 0, icon: 'el-icon-refresh-left', color: '#E6A23C' },
        { label: '退货总金额(元)', value: formatMoney(s.returnTotalAmount), icon: 'el-icon-coin', color: '#F56C6C' }
      ]
    }
  },
  mounted() {
    this.chart = echarts.init(this.$refs.chartEl)
    this.returnChart = echarts.init(this.$refs.returnChartEl)
    // 并排布局下等栅格宽度确定后校正一次图表尺寸
    this.$nextTick(this.handleResize)
    window.addEventListener('resize', this.handleResize)
    this.loadData()
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.chart) {
      this.chart.dispose()
      this.chart = null
    }
    if (this.returnChart) {
      this.returnChart.dispose()
      this.returnChart = null
    }
    if (this.largeChart) {
      this.largeChart.dispose()
      this.largeChart = null
    }
  },
  methods: {
    handleResize() {
      this.chart && this.chart.resize()
      this.returnChart && this.returnChart.resize()
      this.largeChart && this.largeChart.resize()
    },
    async loadData() {
      if (!this.dateRange || this.dateRange.length !== 2) {
        this.$message.warning('请选择起止日期')
        return
      }
      this.loading = true
      try {
        const params = { startDate: this.dateRange[0], endDate: this.dateRange[1] }
        // 并行加载采购趋势、退货趋势与采购/退货汇总
        const [daily, returnDaily, summary] = await Promise.all([
          getDailyPurchase(params),
          getDailyPurchaseReturn(params),
          getPurchaseSummary(params)
        ])
        this.summary = summary || {}
        this.purchaseDaily = daily || []
        this.returnDaily = returnDaily || []
        this.render(this.purchaseDaily)
        this.renderReturn(this.returnDaily)
        // 放大弹窗若处于打开状态,同步刷新大图数据
        if (this.largeVisible) {
          this.renderLargeChart()
        }
        // 数据渲染后再校正一次, 防止并排容器初始宽度未定时图表尺寸偏差
        this.$nextTick(this.handleResize)
      } catch (e) { /* 忽略 */ } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.loadData()
    },
    /** 重置筛选:日期区间恢复为默认近30天并重新加载数据 */
    handleReset() {
      this.dateRange = getDefaultRange()
      this.loadData()
    },
    /**
     * 构建双柱状图配置(金额柱-左轴 + 单数柱-右轴), 采购/退货图共用保证样式一致
     * @param list 按日统计数据
     * @param amountName 金额系列名称
     * @param countName 单数系列名称
     * @param amountColor 金额柱颜色
     */
    buildBarOption(list, amountName, countName, amountColor) {
      const dates = list.map(i => i.statDate)
      const amounts = list.map(i => Number(i.totalAmount) || 0)
      const counts = list.map(i => Number(i.orderCount) || 0)
      return {
        tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
        legend: { data: [amountName, countName] },
        grid: { left: 60, right: 60, top: 50, bottom: 60 },
        xAxis: [{ type: 'category', data: dates, axisLabel: { rotate: 30 } }],
        yAxis: [
          { type: 'value', name: '金额(元)', axisLabel: { formatter: '{value}' } },
          { type: 'value', name: '单数', axisLabel: { formatter: '{value}' } }
        ],
        series: [
          {
            name: amountName,
            type: 'bar',
            data: amounts,
            itemStyle: { color: amountColor }
          },
          {
            name: countName,
            type: 'bar',
            yAxisIndex: 1,
            data: counts,
            itemStyle: { color: '#E6A23C' }
          }
        ]
      }
    },
    render(list) {
      // 采购趋势:蓝色金额柱 + 橙色单数柱
      this.chart && this.chart.setOption(this.buildBarOption(list, '采购金额', '采购单数', '#409EFF'), true)
    },
    renderReturn(list) {
      // 退货趋势:红色金额柱 + 橙色单数柱, 配置结构与采购趋势完全一致
      this.returnChart && this.returnChart.setOption(this.buildBarOption(list, '退货金额', '退货单数', '#F56C6C'), true)
    },
    /**
     * 打开图表放大弹窗
     * @param type 图表类型:purchase-采购趋势, return-退货趋势
     */
    openLargeChart(type) {
      this.largeType = type
      this.largeTitle = type === 'return' ? '采购退货趋势' : '采购趋势'
      this.largeVisible = true
    },
    /** 弹窗打开动画结束后初始化大图并渲染(此时容器宽高已确定) */
    renderLargeChart() {
      if (!this.$refs.largeChartEl) {
        return
      }
      if (!this.largeChart) {
        this.largeChart = echarts.init(this.$refs.largeChartEl)
      }
      const isReturn = this.largeType === 'return'
      const option = isReturn
        ? this.buildBarOption(this.returnDaily, '退货金额', '退货单数', '#F56C6C')
        : this.buildBarOption(this.purchaseDaily, '采购金额', '采购单数', '#409EFF')
      this.largeChart.setOption(option, true)
      this.largeChart.resize()
    },
    /** 弹窗关闭后销毁大图实例,释放资源 */
    disposeLargeChart() {
      if (this.largeChart) {
        this.largeChart.dispose()
        this.largeChart = null
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  .search-bar { margin-bottom: 12px; }
  .summary-row { margin-bottom: 16px; }
  .chart-row { margin-bottom: 16px; }
  .summary-inner { display: flex; align-items: center; }
  .summary-text { margin-left: 16px; }
  .summary-value { font-size: 22px; font-weight: 600; color: #303133; }
  .summary-label { color: #909399; font-size: 13px; margin-top: 4px; }
  .chart { width: 100%; height: 420px; }
  // 图表卡片头部:标题在左, 放大图标在右
  .chart-header { display: flex; justify-content: space-between; align-items: center; }
  .chart-zoom {
    font-size: 18px;
    color: #909399;
    cursor: pointer;
    transition: color 0.2s;
    &:hover { color: #409EFF; }
  }
}
// 放大弹窗 append-to-body 后脱离 .app-container, 需写在嵌套外层(元素仍带 scoped 属性)
.large-chart { width: 100%; height: 70vh; }
</style>
