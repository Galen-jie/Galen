-- ==========================================
-- 宠物救助模块测试数据
-- Author: Galen
-- ==========================================

USE `galen_seckill`;

-- 插入宠物数据
INSERT INTO `pet_rescue` (`pet_name`, `pet_type`, `pet_breed`, `pet_gender`, `pet_age`, `pet_color`, `pet_weight`, `health_status`, `vaccination_status`, `sterilization_status`, `description`, `rescue_location`, `images`, `status`, `publisher_id`) VALUES
('小白', 1, '中华田园犬', 1, '2岁', '白色', 15.00, '健康，已驱虫', 1, 1, '性格温顺，亲人，适合家庭饲养。是在路边救助的流浪狗。', '北京市朝阳区', '["https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=300", "https://images.unsplash.com/photo-1561037404-61cd46aa615b?w=300"]', 0, 1),
('小花', 2, '橘猫', 2, '1岁', '橘色', 4.50, '健康活泼', 1, 0, '非常粘人的小橘猫，喜欢被抚摸。在小区救助的流浪猫。', '上海市浦东新区', '["https://images.unsplash.com/photo-1574158622682-e40e69881006?w=300"]', 0, 1),
('大黄', 1, '金毛', 1, '3岁', '金黄色', 28.00, '健康，已绝育', 1, 1, '金毛犬，性格非常温顺，喜欢和小朋友玩。因主人搬家无法继续饲养。', '广州市天河区', '["https://images.unsplash.com/photo-1552053831-71594a27632d?w=300"]', 0, 1),
('咪咪', 2, '英短蓝猫', 2, '6个月', '蓝灰色', 3.00, '健康', 0, 0, '活泼可爱的小蓝猫，喜欢玩逗猫棒。救助后被暂时寄养。', '深圳市南山区', '["https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=300"]', 0, 1);

-- 更新一些秒杀商品为进行中状态
UPDATE `seckill_goods` SET `status` = 1, `start_time` = DATE_SUB(NOW(), INTERVAL 1 HOUR), `end_time` = DATE_ADD(NOW(), INTERVAL 2 HOUR) WHERE `id` IN (1, 2);

-- 更新一些秒杀商品为即将开始
UPDATE `seckill_goods` SET `status` = 0, `start_time` = DATE_ADD(NOW(), INTERVAL 30 MINUTE), `end_time` = DATE_ADD(NOW(), INTERVAL 3 HOUR) WHERE `id` IN (3, 4);