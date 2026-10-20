// 静态路由: hash 模式, 所有业务页全量注册
import Vue from 'vue'
import VueRouter from 'vue-router'
import Layout from '@/layout/index.vue'

Vue.use(VueRouter)

export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    hidden: true
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'el-icon-s-home', affix: true }
      }
    ]
  },
  {
    path: '/base',
    component: Layout,
    redirect: '/base/product/product',
    meta: { title: '基础资料', icon: 'el-icon-folder', alwaysShow: true },
    children: [
      {
        path: 'product',
        name: 'ProductParent',
        component: () => import('@/layout/components/ParentView.vue'),
        meta: { title: '商品管理' },
        children: [
          { path: 'productCategory', name: 'ProductCategory', component: () => import('@/views/base/productCategory.vue'), meta: { title: '商品分类' } },
          { path: 'product', name: 'Product', component: () => import('@/views/base/product.vue'), meta: { title: '商品管理' } }
        ]
      },
      {
        path: 'supplier',
        name: 'SupplierParent',
        component: () => import('@/layout/components/ParentView.vue'),
        meta: { title: '供应商管理' },
        children: [
          { path: 'supplierCategory', name: 'SupplierCategory', component: () => import('@/views/base/supplierCategory.vue'), meta: { title: '供应商分类' } },
          { path: 'supplier', name: 'Supplier', component: () => import('@/views/base/supplier.vue'), meta: { title: '供应商管理' } }
        ]
      },
      {
        path: 'customer',
        name: 'CustomerParent',
        component: () => import('@/layout/components/ParentView.vue'),
        meta: { title: '客户管理' },
        children: [
          { path: 'customerCategory', name: 'CustomerCategory', component: () => import('@/views/base/customerCategory.vue'), meta: { title: '客户分类' } },
          { path: 'customer', name: 'Customer', component: () => import('@/views/base/customer.vue'), meta: { title: '客户管理' } }
        ]
      },
      { path: 'warehouse', name: 'Warehouse', component: () => import('@/views/base/warehouse.vue'), meta: { title: '仓库管理' } }
    ]
  },
  {
    path: '/purchase',
    component: Layout,
    redirect: '/purchase/order',
    meta: { title: '采购管理', icon: 'el-icon-shopping-cart-full', alwaysShow: true },
    children: [
      { path: 'order', name: 'PurchaseOrder', component: () => import('@/views/purchase/order.vue'), meta: { title: '采购入库' } },
      { path: 'return', name: 'PurchaseReturn', component: () => import('@/views/purchase/return.vue'), meta: { title: '采购退货' } }
    ]
  },
  {
    path: '/sale',
    component: Layout,
    redirect: '/sale/order',
    meta: { title: '销售管理', icon: 'el-icon-sell', alwaysShow: true },
    children: [
      { path: 'order', name: 'SaleOrder', component: () => import('@/views/sale/order.vue'), meta: { title: '销售出库' } },
      { path: 'return', name: 'SaleReturn', component: () => import('@/views/sale/return.vue'), meta: { title: '销售退货' } }
    ]
  },
  {
    path: '/stock',
    component: Layout,
    redirect: '/stock/list',
    meta: { title: '库存管理', icon: 'el-icon-box', alwaysShow: true },
    children: [
      { path: 'list', name: 'StockList', component: () => import('@/views/stock/list.vue'), meta: { title: '库存查询' } },
      { path: 'transfer', name: 'StockTransfer', component: () => import('@/views/stock/transfer.vue'), meta: { title: '库存调拨' } },
      { path: 'warning', name: 'StockWarning', component: () => import('@/views/stock/warning.vue'), meta: { title: '库存预警' } },
      { path: 'check', name: 'StockCheck', component: () => import('@/views/stock/check.vue'), meta: { title: '库存盘点' } }
    ]
  },
  {
    path: '/stats',
    component: Layout,
    redirect: '/stats/purchase',
    meta: { title: '数据统计', icon: 'el-icon-data-line', alwaysShow: true },
    children: [
      { path: 'purchase', name: 'StatsPurchase', component: () => import('@/views/stats/purchase.vue'), meta: { title: '采购统计' } },
      { path: 'sale', name: 'StatsSale', component: () => import('@/views/stats/sale.vue'), meta: { title: '销售统计' } },
      { path: 'stock', name: 'StatsStock', component: () => import('@/views/stats/stock.vue'), meta: { title: '库存统计' } }
    ]
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'el-icon-setting', alwaysShow: true },
    children: [
      { path: 'user', name: 'SystemUser', component: () => import('@/views/system/user.vue'), meta: { title: '用户管理' } },
      { path: 'role', name: 'SystemRole', component: () => import('@/views/system/role.vue'), meta: { title: '角色管理' } },
      { path: 'menu', name: 'SystemMenu', component: () => import('@/views/system/menu.vue'), meta: { title: '菜单管理' } },
      { path: 'log', name: 'SystemLog', component: () => import('@/views/system/log.vue'), meta: { title: '操作日志' } }
    ]
  },
  { path: '*', redirect: '/dashboard', hidden: true }
]

const router = new VueRouter({
  mode: 'hash',
  routes: constantRoutes
})

export default router
