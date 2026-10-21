-- ============================================================
-- 数据库升级脚本 2026-10-21
-- 背景: 建表脚本 jxc_db.sql 长期落后于实体类,导致新环境按脚本建库后
--       登录/商品/采购销售等模块报 "Unknown column" (后端显示"系统异常")。
-- 适用: 由旧版 jxc_db.sql 建出来的库(如开发成员本地库)。
-- 注意: 每条只执行一次,重复执行会报 Duplicate column / Table exists。
-- ============================================================

-- 1. sys_user 性别字段 (实体 SysUser.gender, 登录查询涉及,缺失会导致无法登录)
ALTER TABLE `sys_user` ADD COLUMN `gender` TINYINT DEFAULT NULL COMMENT '性别:1-男,2-女' AFTER `real_name`;

-- 2. 商品 大单位/换算率 (实体 Product.bigUnit / Product.conversionRate)
ALTER TABLE `product` ADD COLUMN `big_unit` VARCHAR(32) DEFAULT NULL COMMENT '大单位,空则无大单位' AFTER `unit`;
ALTER TABLE `product` ADD COLUMN `conversion_rate` INT DEFAULT NULL COMMENT '换算率:1大单位=N基本单位' AFTER `big_unit`;

-- 3. 采购/销售明细 录入数量 (实体 *OrderDetail.inputQuantity)
ALTER TABLE `purchase_order_detail` ADD COLUMN `input_quantity` INT DEFAULT NULL COMMENT '录入数量(按录入单位)' AFTER `unit`;
ALTER TABLE `sale_order_detail` ADD COLUMN `input_quantity` INT DEFAULT NULL COMMENT '录入数量(按录入单位)' AFTER `unit`;

-- 4. 采购退货主表 purchase_return_order
CREATE TABLE `purchase_return_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '退货单号',
  `supplier_id` BIGINT DEFAULT NULL COMMENT '供应商ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '出库仓库ID',
  `order_time` DATETIME DEFAULT NULL COMMENT '退货日期',
  `total_amount` DECIMAL(14,2) DEFAULT 0.00 COMMENT '总金额',
  `total_quantity` INT DEFAULT 0 COMMENT '总数量',
  `refund_amount` DECIMAL(14,2) DEFAULT 0.00 COMMENT '应退金额',
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
  UNIQUE KEY `uk_order_no` (`order_no`, `deleted`),
  KEY `idx_supplier` (`supplier_id`),
  KEY `idx_warehouse` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购退货主表';

-- 5. 采购退货明细表 purchase_return_order_detail
CREATE TABLE `purchase_return_order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '主表ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '退货单号',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_code` VARCHAR(64) DEFAULT NULL COMMENT '商品编码',
  `product_name` VARCHAR(128) DEFAULT NULL COMMENT '商品名称',
  `spec` VARCHAR(128) DEFAULT NULL COMMENT '规格',
  `unit` VARCHAR(32) DEFAULT NULL COMMENT '单位',
  `input_quantity` INT DEFAULT NULL COMMENT '录入数量(按录入单位)',
  `quantity` INT NOT NULL DEFAULT 0 COMMENT '库存数量(换算为基本单位后的数量)',
  `price` DECIMAL(12,2) DEFAULT 0.00 COMMENT '退货单价',
  `amount` DECIMAL(14,2) DEFAULT 0.00 COMMENT '金额',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购退货明细表';

-- 6. 销售退货主表 sale_return_order
CREATE TABLE `sale_return_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '退货单号',
  `customer_id` BIGINT DEFAULT NULL COMMENT '客户ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '入库仓库ID',
  `order_time` DATETIME DEFAULT NULL COMMENT '退货日期',
  `total_amount` DECIMAL(14,2) DEFAULT 0.00 COMMENT '总金额',
  `total_quantity` INT DEFAULT 0 COMMENT '总数量',
  `refund_amount` DECIMAL(14,2) DEFAULT 0.00 COMMENT '应退金额',
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
  UNIQUE KEY `uk_order_no` (`order_no`, `deleted`),
  KEY `idx_customer` (`customer_id`),
  KEY `idx_warehouse` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售退货主表';

-- 7. 销售退货明细表 sale_return_order_detail
CREATE TABLE `sale_return_order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '主表ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '退货单号',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_code` VARCHAR(64) DEFAULT NULL COMMENT '商品编码',
  `product_name` VARCHAR(128) DEFAULT NULL COMMENT '商品名称',
  `spec` VARCHAR(128) DEFAULT NULL COMMENT '规格',
  `unit` VARCHAR(32) DEFAULT NULL COMMENT '单位',
  `input_quantity` INT DEFAULT NULL COMMENT '录入数量(按录入单位)',
  `quantity` INT NOT NULL DEFAULT 0 COMMENT '库存数量(换算为基本单位后的数量)',
  `price` DECIMAL(12,2) DEFAULT 0.00 COMMENT '退货单价',
  `amount` DECIMAL(14,2) DEFAULT 0.00 COMMENT '金额',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售退货明细表';
