<template>
  <div class="app-container">
    <!-- 顶部工具栏 -->
    <el-form :inline="true" size="small" class="search-bar">
      <el-form-item>
        <el-button type="primary" icon="el-icon-refresh" @click="loadData">刷新</el-button>
      </el-form-item>
    </el-form>

    <!-- 汇总统计卡片 -->
    <el-row :gutter="16" v-loading="loading" class="summary-row">
      <el-col :span="12" v-for="card in cards" :key="card.label">
        <el-card shadow="never" class="summary-card">
          <div class="summary-inner">
            <div class="summary-icon" :style="{ background: card.color }">
              <i :class="card.icon"></i>
            </div>
            <div class="summary-text">
              <div class="summary-value">{{ card.value }}</div>
              <div class="summary-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-card shadow="never" class="section-card">
      <div slot="header" class="section-header"><span>各仓库库存统计</span></div>
      <div ref="chartEl" class="chart" v-loading="loading"></div>
    </el-card>

    <!-- 明细表格 -->
    <el-card shadow="never" class="section-card">
      <div slot="header" class="section-header"><span>明细列表</span></div>
      <el-table :data="tableData" border stripe size="small">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="warehouseName" label="仓库" min-width="160" />
        <el-table-column prop="productCount" label="商品种类数" width="120" align="right" />
        <el-table-column prop="totalQuantity" label="库存总数量" width="120" align="right" />
        <el-table-column label="库存总成本" width="140" align="right">
          <template slot-scope="{ row }">{{ formatMoney(row.totalCost) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getStockByWarehouse } from '@/api/stats'
import { formatMoney } from '@/utils/format'

export default {
  name: 'StatsStock',
  data() {
    return {
      loading: false,
      chart: null,
      tableData: []
    }
  },
  computed: {
    // 汇总卡片:库存总数量/库存总成本(按各仓库明细聚合, 口径与明细表一致, 价值按成本价计算)
    cards() {
      let totalQuantity = 0
      let totalValue = 0
      this.tableData.forEach(i => {
        totalQuantity += Number(i.totalQuantity) || 0
        totalValue += Number(i.totalCost) || 0
      })
      return [
        { label: '库存总数量', value: totalQuantity, icon: 'el-icon-box', color: '#409EFF' },
        { label: '库存总成本(元)', value: formatMoney(totalValue), icon: 'el-icon-coin', color: '#E6A23C' }
      ]
    }
  },
  mounted() {
    this.chart = echarts.init(this.$refs.chartEl)
    window.addEventListener('resize', this.handleResize)
    this.loadData()
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.chart) {
      this.chart.dispose()
      this.chart = null
    }
  },
  methods: {
    formatMoney,
    handleResize() {
      this.chart && this.chart.resize()
    },
    async loadData() {
      this.loading = true
      try {
        const data = await getStockByWarehouse()
        this.tableData = data || []
        this.render(this.tableData)
      } catch (e) { /* 忽略 */ } finally {
        this.loading = false
      }
    },
    render(list) {
      const names = list.map(i => i.warehouseName || '-')
      const quantities = list.map(i => Number(i.totalQuantity) || 0)
      const counts = list.map(i => Number(i.productCount) || 0)
      const costs = list.map(i => Number(i.totalCost) || 0)
      const option = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { data: ['库存数量', '商品种类数', '库存总成本'] },
        grid: { left: 60, right: 60, top: 50, bottom: 50 },
        xAxis: [{ type: 'category', data: names, axisLabel: { interval: 0, rotate: 30 } }],
        yAxis: [
          { type: 'value', name: '数量' },
          { type: 'value', name: '金额(元)', axisLabel: { formatter: '{value}' } }
        ],
        series: [
          {
            name: '库存数量',
            type: 'bar',
            data: quantities,
            itemStyle: { color: '#409EFF' },
            barMaxWidth: 40
          },
          {
            name: '商品种类数',
            type: 'bar',
            data: counts,
            itemStyle: { color: '#67C23A' },
            barMaxWidth: 40
          },
          {
            name: '库存总成本',
            type: 'bar',
            // 价值与数量单位不同, 使用右侧金额轴避免量级互相压缩
            yAxisIndex: 1,
            data: costs,
            itemStyle: { color: '#E6A23C' },
            barMaxWidth: 40
          }
        ]
      }
      this.chart && this.chart.setOption(option, true)
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  .search-bar { margin-bottom: 12px; }
  .summary-row { margin-bottom: 16px; }
  .summary-card ::v-deep .el-card__body { padding: 20px; }
  .summary-inner { display: flex; align-items: center; }
  .summary-icon {
    width: 56px; height: 56px;
    border-radius: 8px;
    display: flex; align-items: center; justify-content: center;
    flex-shrink: 0;
    i { font-size: 28px; color: #fff; }
  }
  .summary-text { margin-left: 16px; }
  .summary-value { font-size: 24px; font-weight: 600; color: #303133; }
  .summary-label { color: #909399; font-size: 13px; margin-top: 4px; }
  .section-card { margin-bottom: 16px; }
  .section-header { font-weight: 600; color: #303133; }
  .chart { width: 100%; height: 420px; }
}
</style>
