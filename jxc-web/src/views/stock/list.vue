<template>
  <div class="stock-page">
    <!-- 左侧仓库侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-search">
        <el-input
          v-model="sidebarKeyword"
          placeholder="搜索仓库"
          prefix-icon="el-icon-search"
          size="small"
          clearable
        />
      </div>
      <ul class="warehouse-list">
        <li
          class="warehouse-item"
          :class="{ active: query.warehouseId === undefined }"
          @click="selectWarehouse(undefined)"
        >
          <i class="el-icon-office-building"></i>
          <span>全部仓库</span>
        </li>
        <li
          v-for="w in filteredWarehouses"
          :key="w.id"
          class="warehouse-item"
          :class="{ active: query.warehouseId === w.id }"
          @click="selectWarehouse(w.id)"
        >
          <i class="el-icon-house"></i>
          <span>{{ w.warehouseName }}</span>
        </li>
      </ul>
    </aside>

    <!-- 右侧主区域 -->
    <main class="main-area">
      <!-- 顶部搜索栏 -->
      <div class="top-bar">
        <el-form :inline="true" :model="query" size="small">
          <el-form-item label="商品名称">
            <el-input v-model="query.productName" placeholder="请输入商品名称" clearable
                      @keyup.enter.native="handleSearch" />
          </el-form-item>
          <el-form-item label="商品编码">
            <el-input v-model="query.productCode" placeholder="请输入商品编码" clearable
                      @keyup.enter.native="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
            <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 统计卡片 -->
      <div class="stat-cards">
        <div class="stat-card">
          <div class="stat-label">仓库商品总数</div>
          <div class="stat-value">{{ totalStock }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">商品种类</div>
          <div class="stat-value">{{ productCategoryCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">当前筛选仓库</div>
          <div class="stat-value small">{{ currentWarehouseName }}</div>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="table-panel">
        <div class="panel-title">商品列表</div>
        <el-table v-loading="loading" :data="list" border stripe highlight-current-row @row-click="handleRowClick">
          <el-table-column prop="productCode" label="商品编码" min-width="140" />
          <el-table-column prop="productName" label="商品名称" min-width="160" />
          <el-table-column prop="spec" label="规格型号" min-width="140">
            <template slot-scope="{ row }">{{ row.spec || '-' }}</template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column label="仓库" min-width="120">
            <template slot-scope="{ row }">{{ row.warehouseName || '-' }}</template>
          </el-table-column>
          <el-table-column label="当前库存数量" width="120" align="right">
            <template slot-scope="{ row }">
              <b class="qty-badge">{{ row.quantity }}</b>
            </template>
          </el-table-column>
          <el-table-column label="总仓库数量" width="120" align="right">
            <template slot-scope="{ row }">
              <b class="total-badge">{{ row.totalStock != null ? row.totalStock : row.quantity }}</b>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template slot-scope="{ row }">
              <el-button type="text" icon="el-icon-view" @click.stop="handleFlow(row)">查看</el-button>
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
      </div>
    </main>

    <!-- 库存变动流水弹窗 -->
    <el-dialog
      :visible.sync="flowDialogVisible"
      :title="'库存变动流水 - ' + (flowProduct.productName || '')"
      width="780px"
      @close="handleFlowDialogClose"
    >
      <div v-if="flowDialogVisible">
        <div class="flow-summary">
          <span>商品编码：<b>{{ flowProduct.productCode }}</b></span>
          <span>规格：<b>{{ flowProduct.spec || '-' }}</b></span>
          <span>单位：<b>{{ flowProduct.unit || '-' }}</b></span>
          <span>当前总库存：<b class="total-stock">{{ flowProduct.totalStock != null ? flowProduct.totalStock : flowProduct.quantity }}</b></span>
        </div>
        <el-table v-loading="flowLoading" :data="flowList" border stripe size="small">
          <el-table-column type="index" label="序号" width="55" align="center" />
          <el-table-column label="业务类型" width="100" align="center">
            <template slot-scope="{ row }">
              <el-tag :type="row.direction === 'IN' ? 'success' : 'danger'" size="mini">
                {{ row.orderTypeLabel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="orderNo" label="单据编号" min-width="160" show-overflow-tooltip />
          <el-table-column prop="warehouseName" label="仓库" width="120" />
          <el-table-column v-if="flowList.some(r => r.relatedWarehouseName)" label="关联仓库" width="120">
            <template slot-scope="{ row }">{{ row.relatedWarehouseName || '-' }}</template>
          </el-table-column>
          <el-table-column label="变动数量" width="100" align="right">
            <template slot-scope="{ row }">
              <span :class="row.direction === 'IN' ? 'qty-in' : 'qty-out'">
                {{ row.direction === 'IN' ? '+' : '' }}{{ row.quantity }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="70" align="center" />
          <el-table-column label="变动时间" min-width="160">
            <template slot-scope="{ row }">{{ row.auditTime || row.createTime }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        </el-table>
        <el-pagination
          v-if="flowTotal > 0"
          class="flow-pagination"
          background
          :current-page="flowQuery.page"
          :page-size="flowQuery.size"
          :total="flowTotal"
          layout="total, prev, pager, next"
          small
          @current-change="handleFlowPageChange"
        />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="flowDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getStockPage, getStockFlow, getStockTotal, getProductCategoryCount } from '@/api/stock'
import { getAllWarehouses } from '@/api/warehouse'
import { formatMoney } from '@/utils/format'

export default {
  name: 'StockList',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      productCategoryCount: 0,
      query: { page: 1, size: 10, warehouseId: undefined, productName: '', productCode: '' },
      warehouseList: [],
      sidebarKeyword: '',
      totalStock: 0,
      // 流水弹窗相关
      flowDialogVisible: false,
      flowLoading: false,
      flowProduct: {},
      flowList: [],
      flowTotal: 0,
      flowQuery: { page: 1, size: 10 }
    }
  },
  computed: {
    /** 侧边栏过滤后的仓库列表 */
    filteredWarehouses() {
      if (!this.sidebarKeyword) return this.warehouseList
      return this.warehouseList.filter(w =>
        (w.warehouseName || '').indexOf(this.sidebarKeyword) !== -1
      )
    },
    /** 当前选中的仓库名称 */
    currentWarehouseName() {
      if (this.query.warehouseId === undefined) return '全部仓库'
      const w = this.warehouseList.find(i => i.id === this.query.warehouseId)
      return w ? w.warehouseName : '-'
    }
  },
  created() {
    this.loadOptions()
    this.loadList()
    this.loadTotalStock()
    this.loadProductCategoryCount()
  },
  activated() {
    // 页面被 keep-alive 缓存,再次激活时刷新基础数据(商品/仓库等可能已在其他页面修改)
    this.loadOptions()
  },
  methods: {
    formatMoney,
    async loadTotalStock() {
      try {
        const params = {}
        if (this.query.warehouseId !== undefined && this.query.warehouseId !== null) {
          params.warehouseId = this.query.warehouseId
        }
        const data = await getStockTotal(params)
        this.totalStock = data || 0
      } catch (e) { /* 忽略 */ }
    },
    async loadProductCategoryCount() {
      try {
        const params = {}
        if (this.query.warehouseId !== undefined && this.query.warehouseId !== null) {
          params.warehouseId = this.query.warehouseId
        }
        const data = await getProductCategoryCount(params)
        this.productCategoryCount = data || 0
      } catch (e) { /* 忽略 */ }
    },
    async loadOptions() {
      try {
        this.warehouseList = (await getAllWarehouses()) || []
      } catch (e) { /* 忽略 */ }
    },
    /** 点击左侧仓库切换 */
    selectWarehouse(id) {
      if (this.query.warehouseId === id) return
      this.query.warehouseId = id
      this.query.page = 1
      this.loadList()
      this.loadTotalStock()
      this.loadProductCategoryCount()
    },
    async loadList() {
      this.loading = true
      try {
        const data = await getStockPage(this.query)
        this.list = (data && data.records) || []
        this.total = (data && data.total) || 0
      } catch (e) { /* 忽略 */ } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.query.page = 1
      this.loadList()
      this.loadTotalStock()
    },
    handleReset() {
      this.query = { page: 1, size: 10, warehouseId: undefined, productName: '', productCode: '' }
      this.loadList()
      this.loadTotalStock()
    },
    handleSizeChange(size) {
      this.query.size = size
      this.query.page = 1
      this.loadList()
    },
    handlePageChange(page) {
      this.query.page = page
      this.loadList()
    },
    /** 点击行直接打开流水弹窗 */
    handleRowClick(row) {
      this.handleFlow(row)
    },
    /** 打开商品库存变动流水弹窗 */
    handleFlow(row) {
      this.flowProduct = row
      this.flowQuery = { page: 1, size: 10 }
      this.flowDialogVisible = true
      this.loadFlow(row.productId)
    },
    /** 加载流水数据 */
    async loadFlow(productId) {
      if (!productId) {
        this.flowList = []
        this.flowTotal = 0
        return
      }
      this.flowLoading = true
      try {
        const data = await getStockFlow({ page: this.flowQuery.page, size: this.flowQuery.size, productId })
        this.flowList = (data && data.records) || []
        this.flowTotal = (data && data.total) || 0
      } catch (e) { /* 忽略 */ } finally {
        this.flowLoading = false
      }
    },
    /** 流水分页 */
    handleFlowPageChange(page) {
      this.flowQuery.page = page
      this.loadFlow(this.flowProduct.productId)
    },
    handleFlowDialogClose() {
      this.flowProduct = {}
      this.flowList = []
      this.flowTotal = 0
      this.flowQuery = { page: 1, size: 10 }
    }
  }
}
</script>

<style lang="scss" scoped>
/* ===== 页面整体布局 ===== */
.stock-page {
  display: flex;
  height: calc(100vh - 84px);
  background: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
}

/* ===== 左侧仓库侧边栏 ===== */
.sidebar {
  width: 200px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  padding: 12px 0;

  .sidebar-search {
    padding: 0 12px 10px;
    margin-bottom: 8px;
  }

  .warehouse-list {
    flex: 1;
    overflow-y: auto;
    list-style: none;
    margin: 0;
    padding: 0 8px;

    .warehouse-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px 12px;
      margin-bottom: 4px;
      border-radius: 6px;
      cursor: pointer;
      font-size: 14px;
      color: #606266;
      transition: all 0.2s;

      i { color: #909399; font-size: 16px; }

      &:hover {
        background: #ecf5ff;
        color: #409eff;
        i { color: #409eff; }
      }

      &.active {
        background: #409eff;
        color: #fff;
        font-weight: 600;
        i { color: #fff; }
      }
    }
  }
}

/* ===== 右侧主区域 ===== */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0 16px;
  overflow: hidden;
}

/* 顶部搜索栏 */
.top-bar {
  background: #fff;
  border-radius: 6px;
  padding: 12px 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);

  ::v-deep .el-form-item { margin-bottom: 0; }
}

/* 统计卡片 */
.stat-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;

  .stat-card {
    flex: 1;
    background: #fff;
    border-radius: 6px;
    padding: 16px 20px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.04);
    display: flex;
    flex-direction: column;
    justify-content: center;

    .stat-label {
      font-size: 13px;
      color: #909399;
      margin-bottom: 6px;
    }

    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #303133;
      line-height: 1;

      &.small {
        font-size: 18px;
      }
    }

    &:first-child .stat-value { color: #409eff; }
    &:nth-child(2) .stat-value { color: #67c23a; }
    &:nth-child(3) .stat-value { color: #e6a23c; }
  }
}

/* 表格面板 */
.table-panel {
  flex: 1;
  background: #fff;
  border-radius: 6px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);

  .panel-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
    padding-left: 10px;
    border-left: 3px solid #409eff;
  }

  ::v-deep .el-table {
    flex: 1;
  }

  .pagination {
    margin-top: 12px;
    text-align: right;
  }
}

/* 总库存徽章 */
.total-badge {
  display: inline-block;
  min-width: 36px;
  padding: 2px 8px;
  background: #fef0f0;
  color: #f56c6c;
  border-radius: 4px;
  font-weight: 600;
}
/* 库存数量徽章 */
.qty-badge {
  display: inline-block;
  min-width: 36px;
  padding: 2px 8px;
  background: #f0f9eb;
  color: #67c23a;
  border-radius: 4px;
  font-weight: 600;
}

/* ===== 流水弹窗 ===== */
.total-stock {
  color: #f56c6c;
  font-size: 14px;
}
.qty-in { color: #67c23a; font-weight: 600; }
.qty-out { color: #f56c6c; font-weight: 600; }
.flow-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-bottom: 10px;
  font-size: 13px;
  color: #606266;
  b { color: #303133; margin-left: 4px; }
}
.flow-pagination {
  margin-top: 10px;
  text-align: right;
}

/* 操作列不换行 */
::v-deep .el-table .cell {
  white-space: nowrap;
}
</style>
