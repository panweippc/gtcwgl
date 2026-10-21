-- ============================================================
-- 进销存管理系统数据库脚本 jxc_db
-- MySQL 8.0, charset utf8mb4
-- 管理员账号: admin / admin123
-- ============================================================

DROP DATABASE IF EXISTS `jxc_db`;
CREATE DATABASE `jxc_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `jxc_db`;

-- ----------------------------
-- 1. 系统用户表 sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名',
  `password` VARCHAR(128) NOT NULL COMMENT '密码(MD5+盐)',
  `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(64) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `role_id` BIGINT DEFAULT NULL COMMENT '角色ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`, `deleted`),
  KEY `idx_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ----------------------------
-- 2. 角色表 sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(64) NOT NULL COMMENT '角色编码',
  `data_scope` TINYINT NOT NULL DEFAULT 1 COMMENT '数据范围:1-全部,2-本人',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- 3. 菜单表 sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID',
  `menu_name` VARCHAR(64) NOT NULL COMMENT '菜单名称',
  `menu_type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型:1-目录,2-菜单,3-按钮',
  `path` VARCHAR(128) DEFAULT NULL COMMENT '路由路径',
  `component` VARCHAR(128) DEFAULT NULL COMMENT '前端组件',
  `perms` VARCHAR(128) DEFAULT NULL COMMENT '权限标识',
  `icon` VARCHAR(64) DEFAULT NULL COMMENT '图标',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见:0-隐藏,1-显示',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ----------------------------
-- 4. 角色菜单关联表 sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ----------------------------
-- 5. 操作日志表 sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `module` VARCHAR(64) DEFAULT NULL COMMENT '模块',
  `operation` VARCHAR(128) DEFAULT NULL COMMENT '操作内容',
  `method` VARCHAR(255) DEFAULT NULL COMMENT '请求方法',
  `request_url` VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
  `request_param` TEXT COMMENT '请求参数',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT 'HTTP方法',
  `user_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `username` VARCHAR(64) DEFAULT NULL COMMENT '操作人用户名',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
  `cost_time` BIGINT DEFAULT 0 COMMENT '耗时(毫秒)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-失败,1-成功',
  `error_msg` TEXT COMMENT '错误信息',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ----------------------------
-- 6. 商品分类表 product_category
-- ----------------------------
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
  `category_name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `category_code` VARCHAR(64) DEFAULT NULL COMMENT '分类编码',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ----------------------------
-- 7. 商品表 product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_code` VARCHAR(64) NOT NULL COMMENT '商品编码',
  `product_name` VARCHAR(128) NOT NULL COMMENT '商品名称',
  `category_id` BIGINT DEFAULT NULL COMMENT '商品分类ID',
  `spec` VARCHAR(128) DEFAULT NULL COMMENT '规格',
  `unit` VARCHAR(32) DEFAULT NULL COMMENT '单位',
  `barcode` VARCHAR(64) DEFAULT NULL COMMENT '条形码',
  `expire_date` DATE DEFAULT NULL COMMENT '保质期至(到期日)',
  `cost_price` DECIMAL(12,2) DEFAULT 0.00 COMMENT '成本价',
  `sale_price` DECIMAL(12,2) DEFAULT 0.00 COMMENT '销售价',
  `min_stock` INT DEFAULT 0 COMMENT '最低库存预警',
  `max_stock` INT DEFAULT 0 COMMENT '最高库存预警',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-停售,1-启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`, `deleted`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ----------------------------
-- 8. 供应商分类表 supplier_category
-- ----------------------------
DROP TABLE IF EXISTS `supplier_category`;
CREATE TABLE `supplier_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
  `category_name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `category_code` VARCHAR(64) DEFAULT NULL COMMENT '分类编码',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商分类表';

-- ----------------------------
-- 9. 供应商表 supplier
-- ----------------------------
DROP TABLE IF EXISTS `supplier`;
CREATE TABLE `supplier` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supplier_code` VARCHAR(64) NOT NULL COMMENT '供应商编码',
  `supplier_name` VARCHAR(128) NOT NULL COMMENT '供应商名称',
  `category_id` BIGINT DEFAULT NULL COMMENT '供应商分类ID',
  `contact_person` VARCHAR(64) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `email` VARCHAR(64) DEFAULT NULL COMMENT '邮箱',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `bank_account` VARCHAR(64) DEFAULT NULL COMMENT '银行账号',
  `bank_name` VARCHAR(64) DEFAULT NULL COMMENT '开户行',
  `level` TINYINT DEFAULT 1 COMMENT '等级:1-普通,2-优质,3-战略',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`, `deleted`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- ----------------------------
-- 10. 客户分类表 customer_category
-- ----------------------------
DROP TABLE IF EXISTS `customer_category`;
CREATE TABLE `customer_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
  `category_name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `category_code` VARCHAR(64) DEFAULT NULL COMMENT '分类编码',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户分类表';

-- ----------------------------
-- 11. 客户表 customer
-- ----------------------------
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `customer_code` VARCHAR(64) NOT NULL COMMENT '客户编码',
  `customer_name` VARCHAR(128) NOT NULL COMMENT '客户名称',
  `category_id` BIGINT DEFAULT NULL COMMENT '客户分类ID',
  `contact_person` VARCHAR(64) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `email` VARCHAR(64) DEFAULT NULL COMMENT '邮箱',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `bank_account` VARCHAR(64) DEFAULT NULL COMMENT '银行账号',
  `bank_name` VARCHAR(64) DEFAULT NULL COMMENT '开户行',
  `level` TINYINT DEFAULT 1 COMMENT '等级:1-普通,2-VIP,3-战略',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_code` (`customer_code`, `deleted`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- ----------------------------
-- 12. 仓库表 warehouse
-- ----------------------------
DROP TABLE IF EXISTS `warehouse`;
CREATE TABLE `warehouse` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_code` VARCHAR(64) NOT NULL COMMENT '仓库编码',
  `warehouse_name` VARCHAR(128) NOT NULL COMMENT '仓库名称',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `manager` VARCHAR(64) DEFAULT NULL COMMENT '负责人',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_code` (`warehouse_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

-- ----------------------------
-- 13. 采购入库主表 purchase_order
-- ----------------------------
DROP TABLE IF EXISTS `purchase_order`;
CREATE TABLE `purchase_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '单据编号',
  `supplier_id` BIGINT DEFAULT NULL COMMENT '供应商ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '入库仓库ID',
  `order_time` DATETIME DEFAULT NULL COMMENT '采购日期',
  `total_amount` DECIMAL(14,2) DEFAULT 0.00 COMMENT '总金额',
  `total_quantity` INT DEFAULT 0 COMMENT '总数量',
  `pay_amount` DECIMAL(14,2) DEFAULT 0.00 COMMENT '已付金额',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态:0-待审核,1-已审核',
  `audit_user_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `operator_id` BIGINT DEFAULT NULL COMMENT '业务员ID',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`, `deleted`),
  KEY `idx_supplier` (`supplier_id`),
  KEY `idx_warehouse` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购入库主表';

-- ----------------------------
-- 14. 采购入库明细表 purchase_order_detail
-- ----------------------------
DROP TABLE IF EXISTS `purchase_order_detail`;
CREATE TABLE `purchase_order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '主表ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '单据编号',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_code` VARCHAR(64) DEFAULT NULL COMMENT '商品编码',
  `product_name` VARCHAR(128) DEFAULT NULL COMMENT '商品名称',
  `spec` VARCHAR(128) DEFAULT NULL COMMENT '规格',
  `unit` VARCHAR(32) DEFAULT NULL COMMENT '单位',
  `quantity` INT NOT NULL DEFAULT 0 COMMENT '采购数量',
  `price` DECIMAL(12,2) DEFAULT 0.00 COMMENT '采购单价',
  `amount` DECIMAL(14,2) DEFAULT 0.00 COMMENT '金额',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购入库明细表';

-- ----------------------------
-- 15. 销售出库主表 sale_order
-- ----------------------------
DROP TABLE IF EXISTS `sale_order`;
CREATE TABLE `sale_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '单据编号',
  `customer_id` BIGINT DEFAULT NULL COMMENT '客户ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '出库仓库ID',
  `order_time` DATETIME DEFAULT NULL COMMENT '销售日期',
  `total_amount` DECIMAL(14,2) DEFAULT 0.00 COMMENT '总金额',
  `total_quantity` INT DEFAULT 0 COMMENT '总数量',
  `received_amount` DECIMAL(14,2) DEFAULT 0.00 COMMENT '已收金额',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态:0-待审核,1-已审核',
  `audit_user_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `operator_id` BIGINT DEFAULT NULL COMMENT '业务员ID',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`, `deleted`),
  KEY `idx_customer` (`customer_id`),
  KEY `idx_warehouse` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售出库主表';

-- ----------------------------
-- 16. 销售出库明细表 sale_order_detail
-- ----------------------------
DROP TABLE IF EXISTS `sale_order_detail`;
CREATE TABLE `sale_order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '主表ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '单据编号',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_code` VARCHAR(64) DEFAULT NULL COMMENT '商品编码',
  `product_name` VARCHAR(128) DEFAULT NULL COMMENT '商品名称',
  `spec` VARCHAR(128) DEFAULT NULL COMMENT '规格',
  `unit` VARCHAR(32) DEFAULT NULL COMMENT '单位',
  `quantity` INT NOT NULL DEFAULT 0 COMMENT '销售数量',
  `price` DECIMAL(12,2) DEFAULT 0.00 COMMENT '销售单价',
  `amount` DECIMAL(14,2) DEFAULT 0.00 COMMENT '金额',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售出库明细表';

-- ----------------------------
-- 17. 库存表 stock
-- ----------------------------
DROP TABLE IF EXISTS `stock`;
CREATE TABLE `stock` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `quantity` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
  `lock_quantity` INT NOT NULL DEFAULT 0 COMMENT '锁定数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_product` (`warehouse_id`, `product_id`, `deleted`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- ----------------------------
-- 18. 库存调拨主表 stock_transfer
-- ----------------------------
DROP TABLE IF EXISTS `stock_transfer`;
CREATE TABLE `stock_transfer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '单据编号',
  `from_warehouse_id` BIGINT NOT NULL COMMENT '调出仓库ID',
  `to_warehouse_id` BIGINT NOT NULL COMMENT '调入仓库ID',
  `order_time` DATETIME DEFAULT NULL COMMENT '调拨日期',
  `total_quantity` INT DEFAULT 0 COMMENT '总数量',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态:0-待审核,1-已审核',
  `audit_user_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`, `deleted`),
  KEY `idx_from_wh` (`from_warehouse_id`),
  KEY `idx_to_wh` (`to_warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调拨主表';

-- ----------------------------
-- 19. 库存调拨明细表 stock_transfer_detail
-- ----------------------------
DROP TABLE IF EXISTS `stock_transfer_detail`;
CREATE TABLE `stock_transfer_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '主表ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '单据编号',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_code` VARCHAR(64) DEFAULT NULL COMMENT '商品编码',
  `product_name` VARCHAR(128) DEFAULT NULL COMMENT '商品名称',
  `spec` VARCHAR(128) DEFAULT NULL COMMENT '规格',
  `unit` VARCHAR(32) DEFAULT NULL COMMENT '单位',
  `quantity` INT NOT NULL DEFAULT 0 COMMENT '调拨数量',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调拨明细表';

-- ----------------------------
-- 20. 库存盘点主表 stock_check
-- ----------------------------
DROP TABLE IF EXISTS `stock_check`;
CREATE TABLE `stock_check` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '盘点单编号',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `order_time` DATETIME DEFAULT NULL COMMENT '盘点日期',
  `total_book_quantity` INT DEFAULT 0 COMMENT '账面总数量',
  `total_actual_quantity` INT DEFAULT 0 COMMENT '实盘总数量',
  `total_profit_quantity` INT DEFAULT 0 COMMENT '盘盈总数量',
  `total_loss_quantity` INT DEFAULT 0 COMMENT '盘亏总数量',
  `profit_order_no` VARCHAR(64) DEFAULT NULL COMMENT '盘盈单号(盘点时自动生成)',
  `loss_order_no` VARCHAR(64) DEFAULT NULL COMMENT '盘亏单号(盘点时自动生成)',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '盘点状态:0-待盘点,1-已盘点',
  `audit_user_id` BIGINT DEFAULT NULL COMMENT '盘点人ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '盘点时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_warehouse` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点主表';

-- ----------------------------
-- 21. 库存盘点明细表 stock_check_detail
-- ----------------------------
DROP TABLE IF EXISTS `stock_check_detail`;
CREATE TABLE `stock_check_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '主表ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '盘点单编号',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_code` VARCHAR(64) DEFAULT NULL COMMENT '商品编码',
  `product_name` VARCHAR(128) DEFAULT NULL COMMENT '商品名称',
  `spec` VARCHAR(128) DEFAULT NULL COMMENT '规格',
  `unit` VARCHAR(32) DEFAULT NULL COMMENT '单位',
  `book_quantity` INT DEFAULT 0 COMMENT '账面数量',
  `actual_quantity` INT DEFAULT NULL COMMENT '实盘数量(为空则忽略不计差异)',
  `difference` INT DEFAULT 0 COMMENT '差异数量(实盘-账面)',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点明细表';

-- ----------------------------
-- 22. 库存预警配置表 stock_warning_config
-- ----------------------------
DROP TABLE IF EXISTS `stock_warning_config`;
CREATE TABLE `stock_warning_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(64) NOT NULL COMMENT '规则名称',
  `warning_type` TINYINT NOT NULL DEFAULT 1 COMMENT '预警类型:1-最低库存,2-最高库存',
  `default_min` INT DEFAULT 0 COMMENT '默认最低库存',
  `default_max` INT DEFAULT 0 COMMENT '默认最高库存',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预警配置表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 角色:超管
INSERT INTO `sys_role`(`id`, `role_name`, `role_code`, `data_scope`, `sort`, `status`, `remark`, `create_by`, `deleted`) VALUES
(1, '超级管理员', 'ROLE_ADMIN', 1, 1, 1, '拥有全部权限', 1, 0),
(2, '业务员', 'ROLE_OPERATOR', 1, 2, 1, '采购销售业务', 1, 0),
(3, '仓管员', 'ROLE_KEEPER', 1, 3, 1, '库存管理', 1, 0);

-- 管理员账号 admin / admin123 (MD5+盐: 0e9ad3b901ac50f68418f21270eb6b33)
INSERT INTO `sys_user`(`id`, `username`, `password`, `real_name`, `phone`, `email`, `role_id`, `status`, `remark`, `create_by`, `deleted`) VALUES
(1, 'admin', '0e9ad3b901ac50f68418f21270eb6b33', '系统管理员', '13800000000', 'admin@jxc.com', 1, 1, '系统初始化管理员', 1, 0),
(2, 'operator', '0e9ad3b901ac50f68418f21270eb6b33', '业务员张三', '13800000001', NULL, 2, 1, '业务员', 1, 0),
(3, 'keeper', '0e9ad3b901ac50f68418f21270eb6b33', '仓管李四', '13800000002', NULL, 3, 1, '仓管员', 1, 0);

-- 菜单数据
INSERT INTO `sys_menu`(`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `visible`, `status`, `create_by`, `deleted`) VALUES
(1,   0, '系统管理', 1, '/system',     NULL,                 NULL,             'el-icon-setting',    99, 1, 1, 1, 0),
(2,   1, '用户管理', 2, 'user',        'system/user',        'system:user',    'el-icon-user',       1, 1, 1, 1, 0),
(3,   1, '角色管理', 2, 'role',        'system/role',        'system:role',    'el-icon-s-custom',   2, 1, 1, 1, 0),
(4,   1, '菜单管理', 2, 'menu',        'system/menu',        'system:menu',    'el-icon-menu',        3, 1, 1, 1, 0),
(5,   1, '操作日志', 2, 'log',         'system/log',         'system:log',     'el-icon-document',   4, 1, 1, 1, 0),
(10,  0, '基础资料', 1, '/base',       NULL,                 NULL,             'el-icon-folder',     1, 1, 1, 1, 0),
(11, 10, '商品分类', 2, 'productCategory', 'base/productCategory', 'product:category', 'el-icon-c-scale-to-origin', 1, 1, 1, 1, 0),
(12, 10, '商品管理', 2, 'product',     'base/product',      'product:list',   'el-icon-goods',      2, 1, 1, 1, 0),
(13, 10, '供应商分类', 2, 'supplierCategory', 'base/supplierCategory', 'supplier:category', 'el-icon-suitcase', 3, 1, 1, 1, 0),
(14, 10, '供应商管理', 2, 'supplier',   'base/supplier',     'supplier:list',   'el-icon-truck',      4, 1, 1, 1, 0),
(15, 10, '客户分类', 2, 'customerCategory', 'base/customerCategory', 'customer:category', 'el-icon-s-flag', 5, 1, 1, 1, 0),
(16, 10, '客户管理', 2, 'customer',    'base/customer',     'customer:list',   'el-icon-s-promotion', 6, 1, 1, 1, 0),
(17, 10, '仓库管理', 2, 'warehouse',   'base/warehouse',    'warehouse:list',  'el-icon-house',      7, 1, 1, 1, 0),
(20,  0, '采购管理', 1, '/purchase',   NULL,                 NULL,             'el-icon-shopping-cart-full', 2, 1, 1, 1, 0),
(21, 20, '采购入库', 2, 'order',      'purchase/order',     'purchase:list',   'el-icon-shopping-bag', 1, 1, 1, 1, 0),
(30,  0, '销售管理', 1, '/sale',       NULL,                 NULL,             'el-icon-sell',       3, 1, 1, 1, 0),
(31, 30, '销售出库', 2, 'order',       'sale/order',        'sale:list',       'el-icon-sold-out',   1, 1, 1, 1, 0),
(40,  0, '库存管理', 1, '/stock',      NULL,                 NULL,             'el-icon-box',        4, 1, 1, 1, 0),
(41, 40, '库存查询', 2, 'list',       'stock/list',        'stock:list',      'el-icon-files',      1, 1, 1, 1, 0),
(42, 40, '库存调拨', 2, 'transfer',   'stock/transfer',    'stock:transfer',  'el-icon-sort',       2, 1, 1, 1, 0),
(43, 40, '库存预警', 2, 'warning',    'stock/warning',     'stock:warning',   'el-icon-bell',       3, 1, 1, 1, 0),
(44, 40, '库存盘点', 2, 'check',      'stock/check',       'stock:check',     'el-icon-document-checked', 4, 1, 1, 1, 0),
(50,  0, '数据统计', 1, '/stats',      NULL,                 NULL,             'el-icon-data-line',  5, 1, 1, 1, 0),
(51, 50, '采购统计', 2, 'purchase',   'stats/purchase',    'stats:purchase',  'el-icon-pie-chart',  1, 1, 1, 1, 0),
(52, 50, '销售统计', 2, 'sale',       'stats/sale',         'stats:sale',      'el-icon-data-analysis', 2, 1, 1, 1, 0),
(53, 50, '库存统计', 2, 'stock',      'stats/stock',       'stats:stock',     'el-icon-coin',       3, 1, 1, 1, 0);

-- 超管角色绑定全部菜单
INSERT INTO `sys_role_menu`(`role_id`, `menu_id`) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),
(1,10),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),
(1,20),(1,21),
(1,30),(1,31),
(1,40),(1,41),(1,42),(1,43),(1,44),
(1,50),(1,51),(1,52),(1,53);

-- 业务员角色绑定:基础资料+采购+销售+库存查询+统计
INSERT INTO `sys_role_menu`(`role_id`, `menu_id`) VALUES
(2,10),(2,11),(2,12),(2,13),(2,14),(2,15),(2,16),(2,17),
(2,20),(2,21),
(2,30),(2,31),
(2,40),(2,41),
(2,50),(2,51),(2,52),(2,53);

-- 仓管角色绑定:基础资料+库存+统计
INSERT INTO `sys_role_menu`(`role_id`, `menu_id`) VALUES
(3,10),(3,11),(3,12),(3,17),
(3,40),(3,41),(3,42),(3,43),(3,44),
(3,50),(3,53);

-- 默认仓库
INSERT INTO `warehouse`(`id`, `warehouse_code`, `warehouse_name`, `address`, `manager`, `phone`, `sort`, `status`, `create_by`, `deleted`) VALUES
(1, 'WH001', '主仓库', '北京市朝阳区xx路1号', '李四', '13800000002', 1, 1, 1, 0),
(2, 'WH002', '分仓库', '北京市海淀区xx路2号', '王五', '13800000003', 2, 1, 1, 0);

-- 默认库存预警配置
INSERT INTO `stock_warning_config`(`id`, `name`, `warning_type`, `default_min`, `default_max`, `status`, `remark`, `create_by`, `deleted`) VALUES
(1, '默认最低库存预警', 1, 10, 0, 1, '库存低于最低值时预警', 1, 0),
(2, '默认最高库存预警', 2, 0, 1000, 1, '库存高于最高值时预警', 1, 0);

-- ============================================================
-- 完成
-- ============================================================
SELECT 'jxc_db 初始化完成' AS message;
