<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :inline="true" :model="query" class="search-form">
      <el-form-item label="操作菜单">
        <el-input v-model="query.module" placeholder="请输入操作菜单" clearable @keyup.enter.native="handleSearch" />
      </el-form-item>
      <el-form-item label="操作用户">
        <el-input v-model="query.username" placeholder="请输入用户名" clearable @keyup.enter.native="handleSearch" />
      </el-form-item>
      <el-form-item label="操作时间" style="margin-right:8px;">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          :clearable="true"
        />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="query.onlyError" @change="handleSearch">只查询异常信息</el-checkbox>
      </el-form-item>
    </el-form>
    <div class="search-actions">
      <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
      <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
    </div>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (query.page - 1) * query.size + i + 1" />
      <el-table-column label="操作菜单" prop="module" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作用户" prop="username" min-width="120" />
      <el-table-column label="操作" prop="operation" min-width="160" show-overflow-tooltip />
      <el-table-column label="请求方法" prop="requestMethod" width="110" align="center">
        <template slot-scope="{ row }">
          <el-tag size="small" :type="methodTag(row.requestMethod)">{{ row.requestMethod || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="IP" prop="ip" min-width="140" />
      <el-table-column label="耗时(ms)" prop="costTime" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作时间" prop="createTime" min-width="170" />
    </el-table>

    <!-- 分页 -->
    <el-pagination
      background
      :current-page="query.page"
      :page-size="query.size"
      :page-sizes="[10, 20, 50]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top:16px;text-align:right"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<script>
import { getLogPage } from '@/api/system'

export default {
  name: 'SystemLog',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      dateRange: null,
      query: { page: 1, size: 10, module: '', username: '', onlyError: false, startDate: '', endDate: '' }
    }
  },
  created() { this.fetchList() },
  methods: {
    async fetchList() {
      this.loading = true
      try {
        const data = await getLogPage(this.query)
        this.list = (data && data.records) || []
        this.total = (data && data.total) || 0
      } catch (e) { /* 忽略 */ } finally {
        this.loading = false
      }
    },
    handleSearch() {
      if (this.dateRange && this.dateRange.length === 2) {
        this.query.startDate = this.dateRange[0]
        this.query.endDate = this.dateRange[1]
      } else {
        this.query.startDate = ''
        this.query.endDate = ''
      }
      this.query.page = 1
      this.fetchList()
    },
    handleReset() {
      this.dateRange = null
      this.query = { page: 1, size: 10, module: '', username: '', onlyError: false, startDate: '', endDate: '' }
      this.fetchList()
    },
    handlePageChange(p) { this.query.page = p; this.fetchList() },
    handleSizeChange(s) { this.query.size = s; this.query.page = 1; this.fetchList() },
    /** 根据HTTP方法返回对应的el-tag类型 */
    methodTag(m) {
      if (m === 'GET') return 'success'
      if (m === 'POST') return 'warning'
      if (m === 'PUT') return ''
      if (m === 'DELETE') return 'danger'
      return 'info'
    }
  }
}
</script>

<style lang="scss" scoped>
.search-form { margin-bottom: 12px; }
.search-actions { margin-bottom: 12px; }
/* 缩小表单项之间的间距 */
::v-deep .search-form .el-form-item { margin-right: 8px; }
/* 覆盖全局样式：取消最后一项（只查询异常信息）的推右效果，使其紧跟日期选择器 */
::v-deep .search-form .el-form-item:last-child { margin-left: 0 !important; margin-right: 0 !important; }
</style>
