-- ==========================================
-- Galen分布式秒杀系统数据库初始化脚本
-- Author: Galen
-- Date: 2024-01-01
-- ==========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `galen_seckill` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `galen_seckill`;

-- ==========================================
-- 用户表
-- ==========================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `gender` TINYINT(1) DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ==========================================
-- 商品分类表
-- ==========================================
DROP TABLE IF EXISTS `goods_category`;
CREATE TABLE `goods_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父分类ID',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ==========================================
-- 商品表
-- ==========================================
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `goods_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `goods_title` VARCHAR(500) DEFAULT NULL COMMENT '商品标题',
  `goods_img` VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
  `goods_detail` TEXT COMMENT '商品详情',
  `goods_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '商品价格',
  `stock` INT(11) DEFAULT 0 COMMENT '库存数量',
  `category_id` BIGINT(20) DEFAULT NULL COMMENT '分类ID',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ==========================================
-- 秒杀商品表
-- ==========================================
DROP TABLE IF EXISTS `seckill_goods`;
CREATE TABLE `seckill_goods` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '秒杀商品ID',
  `seckill_id` VARCHAR(64) NOT NULL COMMENT '秒杀ID（唯一标识）',
  `goods_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `seckill_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '秒杀价格',
  `stock_count` INT(11) DEFAULT 0 COMMENT '秒杀库存',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束',
  `version` INT(11) DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seckill_id` (`seckill_id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_time_status` (`start_time`, `end_time`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- ==========================================
-- 秒杀订单表
-- ==========================================
DROP TABLE IF EXISTS `seckill_order`;
CREATE TABLE `seckill_order` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `seckill_id` VARCHAR(64) NOT NULL COMMENT '秒杀ID',
  `goods_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `seckill_goods_id` BIGINT(20) NOT NULL COMMENT '秒杀商品ID',
  `goods_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `goods_img` VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
  `order_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '订单金额',
  `status` TINYINT(1) DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消',
  `pay_type` TINYINT(1) DEFAULT NULL COMMENT '支付方式：1-支付宝，2-微信',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收货人电话',
  `receiver_address` VARCHAR(500) DEFAULT NULL COMMENT '收货地址',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  UNIQUE KEY `uk_user_seckill_goods` (`user_id`, `seckill_id`, `goods_id`),
  KEY `idx_user_time` (`user_id`, `create_time`),
  KEY `idx_seckill` (`seckill_id`),
  KEY `idx_status` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

-- ==========================================
-- 库存日志表
-- ==========================================
DROP TABLE IF EXISTS `stock_log`;
CREATE TABLE `stock_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `seckill_id` VARCHAR(64) NOT NULL COMMENT '秒杀ID',
  `order_no` VARCHAR(64) DEFAULT NULL COMMENT '订单号',
  `change_type` TINYINT(1) NOT NULL COMMENT '变动类型：1-扣减，2-回滚',
  `change_count` INT(11) NOT NULL COMMENT '变动数量',
  `before_stock` INT(11) NOT NULL COMMENT '变动前库存',
  `after_stock` INT(11) NOT NULL COMMENT '变动后库存',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_seckill` (`seckill_id`),
  KEY `idx_order` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存日志表';

-- ==========================================
-- 初始化测试数据
-- ==========================================

-- 插入用户数据
INSERT INTO `user` (`username`, `password`, `phone`, `email`, `nickname`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHsM', '13800138000', 'admin@galen.com', '管理员'),
('test1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHsM', '13800138001', 'test1@galen.com', '测试用户1'),
('test2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHsM', '13800138002', 'test2@galen.com', '测试用户2');

-- 插入商品分类数据
INSERT INTO `goods_category` (`category_name`, `parent_id`, `sort`) VALUES
('数码产品', 0, 1),
('手机', 1, 1),
('电脑', 1, 2),
('服装', 0, 2);

-- 插入商品数据
INSERT INTO `goods` (`goods_name`, `goods_title`, `goods_img`, `goods_price`, `stock`, `category_id`) VALUES
('iPhone 15 Pro', '苹果 iPhone 15 Pro 256GB 暗夜紫', 'https://via.placeholder.com/300', 8999.00, 100, 2),
('MacBook Pro 14', '苹果 MacBook Pro 14英寸 M3芯片 16GB 512GB', 'https://via.placeholder.com/300', 14999.00, 50, 3),
('HUAWEI Mate 60 Pro', '华为 Mate 60 Pro 12GB+512GB 雅丹黑', 'https://via.placeholder.com/300', 6999.00, 200, 2),
('小米14', '小米14 徕卡光学镜头 骁龙8Gen3 16GB+512GB', 'https://via.placeholder.com/300', 4999.00, 300, 2);

-- 插入秒杀商品数据
INSERT INTO `seckill_goods` (`seckill_id`, `goods_id`, `seckill_price`, `stock_count`, `start_time`, `end_time`, `status`) VALUES
('SEC001', 1, 6999.00, 50, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY INTERVAL 2 HOUR), 0),
('SEC002', 2, 9999.00, 20, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY INTERVAL 2 HOUR), 0),
('SEC003', 3, 4999.00, 100, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY INTERVAL 2 HOUR), 0),
('SEC004', 4, 2999.00, 150, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY INTERVAL 2 HOUR), 0);

-- 密码说明：以上测试密码均为 123456，使用BCrypt加密