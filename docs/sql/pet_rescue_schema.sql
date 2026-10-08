-- ==========================================
-- 宠物救助模块数据库表
-- Author: Galen
-- Date: 2026-10-08
-- ==========================================

-- 1. 扩展user表，添加角色字段
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `role` TINYINT(1) DEFAULT 0 COMMENT '角色：0-普通用户，1-救助站/志愿者';
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `organization` VARCHAR(100) DEFAULT NULL COMMENT '所属机构';
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `certificate` VARCHAR(255) DEFAULT NULL COMMENT '资质证书URL';

-- 2. 宠物信息表
DROP TABLE IF EXISTS `pet_rescue`;
CREATE TABLE `pet_rescue` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '宠物ID',
  `pet_name` VARCHAR(50) DEFAULT NULL COMMENT '宠物名称',
  `pet_type` TINYINT(1) NOT NULL COMMENT '宠物类型：1-狗，2-猫，3-其他',
  `pet_breed` VARCHAR(100) DEFAULT NULL COMMENT '品种',
  `pet_gender` TINYINT(1) DEFAULT 0 COMMENT '性别：0-未知，1-公，2-母',
  `pet_age` VARCHAR(20) DEFAULT NULL COMMENT '年龄',
  `pet_color` VARCHAR(50) DEFAULT NULL COMMENT '毛色',
  `pet_weight` DECIMAL(5,2) DEFAULT NULL COMMENT '体重(kg)',
  `health_status` VARCHAR(500) DEFAULT NULL COMMENT '健康状况',
  `vaccination_status` TINYINT(1) DEFAULT 0 COMMENT '疫苗接种：0-未接种，1-已接种',
  `sterilization_status` TINYINT(1) DEFAULT 0 COMMENT '绝育状态：0-未绝育，1-已绝育',
  `description` TEXT COMMENT '详细描述',
  `rescue_location` VARCHAR(255) DEFAULT NULL COMMENT '救助地点',
  `rescue_time` DATETIME DEFAULT NULL COMMENT '救助时间',
  `images` VARCHAR(1000) DEFAULT NULL COMMENT '图片URL列表（JSON数组）',
  `video` VARCHAR(255) DEFAULT NULL COMMENT '视频URL',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-待领养，1-已预约，2-已领养，3-已下架',
  `publisher_id` BIGINT(20) NOT NULL COMMENT '发布者ID',
  `view_count` INT(11) DEFAULT 0 COMMENT '浏览次数',
  `like_count` INT(11) DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT(11) DEFAULT 0 COMMENT '评论数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_publisher` (`publisher_id`),
  KEY `idx_status` (`status`),
  KEY `idx_type_status` (`pet_type`, `status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物救助信息表';

-- 3. 领养申请表
DROP TABLE IF EXISTS `adoption_application`;
CREATE TABLE `adoption_application` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `application_no` VARCHAR(64) NOT NULL COMMENT '申请编号',
  `pet_id` BIGINT(20) NOT NULL COMMENT '宠物ID',
  `applicant_id` BIGINT(20) NOT NULL COMMENT '申请人ID',
  `applicant_name` VARCHAR(50) NOT NULL COMMENT '申请人姓名',
  `applicant_phone` VARCHAR(20) NOT NULL COMMENT '申请人电话',
  `applicant_address` VARCHAR(500) DEFAULT NULL COMMENT '申请人地址',
  `applicant_job` VARCHAR(100) DEFAULT NULL COMMENT '职业',
  `living_situation` VARCHAR(500) DEFAULT NULL COMMENT '居住情况（如住房类型、面积等）',
  `experience` TEXT COMMENT '养宠经验',
  `reason` TEXT COMMENT '领养理由',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝，3-已取消',
  `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝理由',
  `reviewer_id` BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_application_no` (`application_no`),
  UNIQUE KEY `uk_pet_applicant` (`pet_id`, `applicant_id`),
  KEY `idx_applicant` (`applicant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领养申请表';

-- 4. 评论表
DROP TABLE IF EXISTS `pet_comment`;
CREATE TABLE `pet_comment` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `pet_id` BIGINT(20) NOT NULL COMMENT '宠物ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '评论用户ID',
  `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父评论ID（0表示一级评论）',
  `reply_user_id` BIGINT(20) DEFAULT NULL COMMENT '回复用户ID',
  `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `like_count` INT(11) DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_pet` (`pet_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物评论表';

-- 5. 宠物点赞表
DROP TABLE IF EXISTS `pet_like`;
CREATE TABLE `pet_like` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `pet_id` BIGINT(20) NOT NULL COMMENT '宠物ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pet_user` (`pet_id`, `user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物点赞表';

-- 6. 评论点赞表
DROP TABLE IF EXISTS `comment_like`;
CREATE TABLE `comment_like` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `comment_id` BIGINT(20) NOT NULL COMMENT '评论ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- 7. 文件上传记录表
DROP TABLE IF EXISTS `file_upload`;
CREATE TABLE `file_upload` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
  `file_url` VARCHAR(500) NOT NULL COMMENT '文件URL',
  `file_size` BIGINT(20) DEFAULT NULL COMMENT '文件大小（字节）',
  `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
  `uploader_id` BIGINT(20) NOT NULL COMMENT '上传者ID',
  `business_type` VARCHAR(50) DEFAULT NULL COMMENT '业务类型（pet_image、pet_video等）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_uploader` (`uploader_id`),
  KEY `idx_type` (`business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件上传记录表';