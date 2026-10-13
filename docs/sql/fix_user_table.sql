-- 修复 user 表缺失的字段
-- 执行时间：2026-10-11
-- 原因：实体类添加了 role, organization, certificate 字段，但数据库表缺失

-- 添加角色字段
ALTER TABLE `user` ADD COLUMN `role` TINYINT(1) DEFAULT 0 COMMENT '角色：0-普通用户，1-救助站/志愿者';

-- 添加所属机构字段
ALTER TABLE `user` ADD COLUMN `organization` VARCHAR(100) DEFAULT NULL COMMENT '所属机构';

-- 添加资质证书字段
ALTER TABLE `user` ADD COLUMN `certificate` VARCHAR(255) DEFAULT NULL COMMENT '资质证书URL';

-- 验证字段是否添加成功
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'user'
  AND COLUMN_NAME IN ('role', 'organization', 'certificate');