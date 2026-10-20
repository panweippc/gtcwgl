-- ============================================================
-- 采购退货 & 销售退货 模块建表 SQL(迁移脚本)
-- 在 jxc_db 库下执行
-- ============================================================
USE `jxc_db`;

-- ----------------------------
-- 采购退货主表
-- ----------------------------
DROP TABLE IF EXISTS `purchase_return_order`;
CREATE TABLE `purchase_return_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '退货单号',
  `supplier_id` BIGINT DEFAULT NULL COMMENT '供应商ID',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '出库仓库ID',
  `order_time` DATETIME DEFAULT NULL COMMENT '退货日期',
  `total_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总金额',
  `total_quantity` INT DEFAULT 0 COMMENT '总数量',
  `refund_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '应退金额',
  `return_reason` VARCHAR(255) DEFAULT NULL COMMENT '退货原因',
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
  UNIQUE KEY `uk_return_order_no` (`order_no`, `deleted`),
  KEY `idx_supplier` (`supplier_id`),
  KEY `idx_warehouse` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购退货主表';

-- ----------------------------
-- 采购退货明细表
-- ----------------------------
DROP TABLE IF EXISTS `purchase_return_order_detail`;
CREATE TABLE `purchase_return_order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '主表ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '退货单号',
  `product_id` BIGINT DEFAULT NULL COMMENT '商品ID',
  `product_code` VARCHAR(64) DEFAULT NULL COMMENT '商品编码',
  `product_name` VARCHAR(128) DEFAULT NULL COMMENT '商品名称',
  `spec` VARCHAR(128) DEFAULT NULL COMMENT '规格',
  `unit` VARCHAR(32) DEFAULT NULL COMMENT '单位',
  `quantity` INT DEFAULT 0 COMMENT '退货数量',
  `price` DECIMAL(12,2) DEFAULT 0.00 COMMENT '退货单价',
  `amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '金额',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购退货明细表';

-- ----------------------------
-- 销售退货主表
-- ----------------------------
DROP TABLE IF EXISTS `sale_return_order`;
CREATE TABLE `sale_return_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '退货单号',
  `customer_id` BIGINT DEFAULT NULL COMMENT '客户ID',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '入库仓库ID',
  `order_time` DATETIME DEFAULT NULL COMMENT '退货日期',
  `total_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总金额',
  `total_quantity` INT DEFAULT 0 COMMENT '总数量',
  `refund_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '应退金额',
  `return_reason` VARCHAR(255) DEFAULT NULL COMMENT '退货原因',
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
  UNIQUE KEY `uk_return_order_no` (`order_no`, `deleted`),
  KEY `idx_customer` (`customer_id`),
  KEY `idx_warehouse` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售退货主表';

-- ----------------------------
-- 销售退货明细表
-- ----------------------------
DROP TABLE IF EXISTS `sale_return_order_detail`;
CREATE TABLE `sale_return_order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '主表ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '退货单号',
  `product_id` BIGINT DEFAULT NULL COMMENT '商品ID',
  `product_code` VARCHAR(64) DEFAULT NULL COMMENT '商品编码',
  `product_name` VARCHAR(128) DEFAULT NULL COMMENT '商品名称',
  `spec` VARCHAR(128) DEFAULT NULL COMMENT '规格',
  `unit` VARCHAR(32) DEFAULT NULL COMMENT '单位',
  `quantity` INT DEFAULT 0 COMMENT '退货数量',
  `price` DECIMAL(12,2) DEFAULT 0.00 COMMENT '退货单价',
  `amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '金额',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售退货明细表';

-- ----------------------------
-- 菜单初始化(采购退货 + 销售退货)
-- 采购管理父菜单假设 id=5, 销售管理父菜单假设 id=8(需按实际调整)
-- ----------------------------
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status) VALUES
(5, '采购退货', 2, 'return', 'purchase/return', 'purchase:return:list', '', 2, 1, 1),
(8, '销售退货', 2, 'return', 'sale/return', 'sale:return:list', '', 2, 1, 1);

-- ============================================================
-- 库存盘点 模块建表 SQL(迁移脚本)
-- 在 jxc_db 库下执行
-- ============================================================

-- ----------------------------
-- 库存盘点主表
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
-- 库存盘点明细表
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
-- 菜单初始化(库存盘点, 父菜单 库存管理 id=40)
-- ----------------------------
INSERT INTO sys_menu (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `visible`, `status`, `create_by`, `deleted`) VALUES
(44, 40, '库存盘点', 2, 'check', 'stock/check', 'stock:check', 'el-icon-document-checked', 4, 1, 1, 1, 0);

-- 角色绑定: 超管(1) + 仓管员(3)
INSERT INTO sys_role_menu (`role_id`, `menu_id`) VALUES (1, 44), (3, 44);
