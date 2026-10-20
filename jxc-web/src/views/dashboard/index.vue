<template>
  <div class="dashboard app-container">
    <!-- 卡片汇总区 -->
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <i :class="card.icon" :style="{ color: card.color, fontSize: '40px' }"></i>
            <div class="stat-text">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 库存预警明细区: 卡片下方 -->
    <el-card v-if="warningList.length > 0" shadow="hover" class="warning-card">
      <div slot="header" class="warning-header">
        <span class="warning-title">
          <i class="el-icon-warning-outline"></i> 库存预警明细
          <el-tag size="mini" type="danger" effect="plain" style="margin-left:8px">
            缺货 {{ shortageCount }}
          </el-tag>
          <el-tag size="mini" type="warning" effect="plain">
            积压 {{ overstockCount }}
          </el-tag>
        </span>
        <el-button type="text" size="small" icon="el-icon-arrow-right" @click="goWarningPage">
          查看全部
        </el-button>
      </div>
      <el-table :data="warningList.slice(0, 4)" border size="small" style="width:100%">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column prop="warehouseName" label="仓库" min-width="120" show-overflow-tooltip />
        <el-table-column prop="productCode" label="编码" width="120" show-overflow-tooltip />
        <el-table-column prop="productName" label="商品" min-width="140" show-overflow-tooltip />
        <el-table-column prop="spec" label="规格" width="100" show-overflow-tooltip />
        <el-table-column label="库存" width="140" align="center">
          <template slot-scope="{ row }">
            <span :class="{ 'qty-danger': row.warningType === 1, 'qty-over': row.warningType === 2 }">
              {{ row.quantity }}
            </span>
            <span class="qty-sep">/</span>
            <span class="threshold">
              最低 {{ row.minStock }}<template v-if="row.maxStock"> / 最高 {{ row.maxStock }}</template>
            </span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="80" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.warningType === 1 ? 'danger' : 'warning'" size="mini">
              {{ row.warningType === 1 ? '缺货' : '积压' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 无预警时显示提示 -->
    <el-card v-else shadow="hover" class="warning-card warning-empty">
      <i class="el-icon-circle-check" style="font-size:28px;color:#67C23A;margin-right:10px"></i>
      <span>当前所有商品库存正常，无预警信息</span>
    </el-card>
  </div>
</template>

<script>
import { getDashboard } from '@/api/stats'
import { getStockWarning } from '@/api/stock'
import { formatMoney } from '@/utils/format'

export default {
  name: 'Dashboard',
  data() {
    return {
      data: {},
      warningList: []
    }
  },
  computed: {
    userInfo() { return this.$store.state.user.userInfo || {} },
    roleName() { return this.userInfo.roleName || '' },
    shortageCount() {
      return this.warningList.filter(w => w.warningType === 1).length
    },
    overstockCount() {
      return this.warningList.filter(w => w.warningType === 2).length
    },
    cards() {
      const d = this.data || {}
      return [
        { label: '采购单数', value: d.purchaseOrderCount || 0, icon: 'el-icon-shopping-bag-2', color: '#E6A23C' },
        { label: '采购总额(元)', value: formatMoney(d.purchaseTotalAmount), icon: 'el-icon-shopping-cart-full', color: '#409EFF' },
        { label: '销售单数', value: d.saleOrderCount || 0, icon: 'el-icon-sold-out', color: '#F56C6C' },
        { label: '销售总额(元)', value: formatMoney(d.saleTotalAmount), icon: 'el-icon-sell', color: '#67C23A' },
        { label: '商品种类数', value: d.productCategoryCount || 0, icon: 'el-icon-goods', color: '#909399' },
        { label: '库存预警数', value: d.stockWarningCount || 0, icon: 'el-icon-bell', color: '#ff9800' },
        { label: '库存总数量', value: d.stockTotalQuantity || 0, icon: 'el-icon-box', color: '#9c27b0' },
        { label: '当前角色', value: this.roleName || '-', icon: 'el-icon-user', color: '#009688' }
      ]
    }
  },
  created() {
    this.loadDashboard()
    this.loadWarning()
  },
  methods: {
    /** 加载首页看板汇总数据 */
    async loadDashboard() {
      try {
        this.data = await getDashboard() || {}
      } catch (e) { /* 忽略 */ }
    },
    /** 加载库存预警明细(用于卡片下方表格) */
    async loadWarning() {
      try {
        this.warningList = (await getStockWarning()) || []
      } catch (e) { /* 忽略 */ }
    },
    /** 跳转到库存预警页面 */
    goWarningPage() {
      this.$router.push('/stock/warning')
    }
  }
}
</script>

<style lang="scss" scoped>
.stat-card { margin-bottom: 16px; }
.stat-inner { display: flex; align-items: center; }
.stat-text { margin-left: 16px; }
.stat-value { font-size: 22px; font-weight: 600; color: #303133; }
.stat-label { color: #909399; font-size: 13px; margin-top: 4px; }

.warning-card { margin-bottom: 16px; }
.warning-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.warning-title { font-size: 15px; font-weight: 600; color: #303133; }
.warning-empty {
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
  padding: 18px 0;
}

/* 库存数量颜色 */
.qty-danger { color: #F56C6C; font-weight: bold; }
.qty-over { color: #E6A23C; font-weight: bold; }
.qty-sep { color: #C0C4CC; margin: 0 4px; }
.threshold { color: #909399; font-size: 12px; }
</style>
