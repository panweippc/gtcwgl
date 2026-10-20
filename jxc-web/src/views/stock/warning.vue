<template>
  <div class="app-container">
    <!-- 工具栏 -->
    <el-form class="search-form" :inline="true" :model="query">
      <el-form-item label="仓库">
        <el-select v-model="query.warehouseId" placeholder="全部仓库" clearable filterable>
          <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="预警类型">
        <el-select v-model="query.warningType" placeholder="全部类型" clearable style="width:140px">
          <el-option label="缺货" :value="1" />
          <el-option label="积压" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 列表 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="warehouseName" label="仓库" min-width="140" />
        <el-table-column prop="productCode" label="商品编码" min-width="140" />
        <el-table-column prop="productName" label="商品名称" min-width="160" />
        <el-table-column prop="spec" label="规格" min-width="120" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column label="当前库存" width="110" align="right">
          <template slot-scope="{ row }">
            <span :class="{ 'stock-danger': row.warningType === 1, 'stock-over': row.warningType === 2 }">
              {{ row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="最低库存" width="100" align="right" />
        <el-table-column prop="maxStock" label="最高库存" width="100" align="right" />
        <el-table-column label="预警类型" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.warningType === 1 ? 'danger' : 'warning'" size="small">
              {{ row.warningType === 1 ? '缺货' : '积压' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" icon="el-icon-view" @click="handleView(row)">查看</el-button>
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

    <!-- 查看预警详情弹窗 -->
    <el-dialog
      title="查看库存预警"
      :visible.sync="viewVisible"
      width="680px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form label-width="110px" class="view-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="仓库:">
              <el-input :value="viewRow.warehouseName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品编码:">
              <el-input :value="viewRow.productCode" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="商品名称:">
              <el-input :value="viewRow.productName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格:">
              <el-input :value="viewRow.spec" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="单位:">
              <el-input :value="viewRow.unit" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前库存:">
              <el-input :value="viewRow.quantity" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="最低库存:">
              <el-input :value="viewRow.minStock" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高库存:">
              <el-input :value="viewRow.maxStock" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="预警类型:">
              <el-tag :type="viewRow.warningType === 1 ? 'danger' : 'warning'" size="small">
                {{ viewRow.warningType === 1 ? '缺货' : '积压' }}
              </el-tag>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer">
        <el-button @click="viewVisible = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getStockWarningPage } from '@/api/stock'
import { getAllWarehouses } from '@/api/warehouse'

export default {
  name: 'StockWarning',
  data() {
    return {
      query: {
        page: 1,
        size: 10,
        warehouseId: undefined,
        warningType: undefined
      },
      loading: false,
      tableData: [],
      total: 0,
      warehouseList: [],
      // 查看弹窗
      viewVisible: false,
      viewRow: {}
    }
  },
  created() {
    this.loadOptions()
    this.loadData()
  },
  activated() {
    // 页面被 keep-alive 缓存,再次激活时刷新基础数据(商品/仓库等可能已在其他页面修改)
    this.loadOptions()
  },
  methods: {
    async loadOptions() {
      try {
        this.warehouseList = (await getAllWarehouses()) || []
      } catch (e) { /* 忽略 */ }
    },
    async loadData() {
      this.loading = true
      try {
        const data = await getStockWarningPage({
          page: this.query.page,
          size: this.query.size,
          warehouseId: this.query.warehouseId,
          warningType: this.query.warningType
        })
        this.tableData = data.records || []
        this.total = data.total || 0
      } catch (e) { /* 忽略 */ } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.query.page = 1
      this.loadData()
    },
    handleReset() {
      this.query.warehouseId = undefined
      this.query.warningType = undefined
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
    /** 查看预警详情 */
    handleView(row) {
      this.viewRow = { ...row }
      this.viewVisible = true
    }
  }
}
</script>

<style lang="scss" scoped>
/* 覆盖全局 search-form 最后一个 el-form-item 推右样式,使查询按钮紧跟预警类型 */
::v-deep .search-form .el-form-item:last-child {
  margin-left: 0 !important;
}
/* 库存低于最低值时标红加粗 */
.stock-danger {
  color: #F56C6C;
  font-weight: bold;
}
/* 库存高于最高值时标橙加粗 */
.stock-over {
  color: #E6A23C;
  font-weight: bold;
}
</style>
