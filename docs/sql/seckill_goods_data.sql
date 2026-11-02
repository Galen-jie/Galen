-- ==========================================
-- 秒杀商品数据（追加）
-- Author: Galen
-- Date: 2026-11-01
-- 说明：
--   1. seckill_id 从 SEC005 开始，避开 schema.sql 中的 SEC001~SEC004
--   2. goods_id 关联 goods 表已有商品（1~4 数码 / 5~52 宠物用品）
--   3. 秒杀价均低于商品原价，库存为秒杀专属库存
--   4. 状态：0-未开始(即将开始) 1-进行中 2-已结束
--   5. 时间使用相对 NOW() 计算，执行后状态即生效；
--      定时任务 updateStatus 会按 start_time/end_time 自动校正状态
-- ==========================================

USE `galen_seckill`;

-- ==========================================
-- 进行中的秒杀（status=1）
-- start_time 已过、end_time 未到
-- ==========================================
INSERT INTO `seckill_goods` (`seckill_id`, `goods_id`, `seckill_price`, `stock_count`, `start_time`, `end_time`, `status`) VALUES
('SEC005', 5,  259.00, 200, DATE_SUB(NOW(), INTERVAL 1 HOUR),  DATE_ADD(NOW(), INTERVAL 3 HOUR), 1),
('SEC006', 6,  459.00, 100, DATE_SUB(NOW(), INTERVAL 2 HOUR),  DATE_ADD(NOW(), INTERVAL 5 HOUR), 1),
('SEC007', 12,  49.00, 300, DATE_SUB(NOW(), INTERVAL 1 HOUR),  DATE_ADD(NOW(), INTERVAL 2 HOUR), 1),
('SEC008', 21,  39.90, 150, DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_ADD(NOW(), INTERVAL 4 HOUR), 1),
('SEC009', 48,  65.00, 120, DATE_SUB(NOW(), INTERVAL 3 HOUR),  DATE_ADD(NOW(), INTERVAL 1 HOUR), 1);

-- ==========================================
-- 即将开始的秒杀（status=0）
-- start_time 在未来
-- ==========================================
INSERT INTO `seckill_goods` (`seckill_id`, `goods_id`, `seckill_price`, `stock_count`, `start_time`, `end_time`, `status`) VALUES
('SEC010', 9,  239.00, 250, DATE_ADD(NOW(), INTERVAL 1 DAY),  DATE_ADD(NOW(), INTERVAL 26 HOUR), 0),
('SEC011', 11,  29.90, 500, DATE_ADD(NOW(), INTERVAL 1 DAY),  DATE_ADD(NOW(), INTERVAL 26 HOUR), 0),
('SEC012', 16,  39.90, 400, DATE_ADD(NOW(), INTERVAL 1 DAY),  DATE_ADD(NOW(), INTERVAL 26 HOUR), 0),
('SEC013', 17,   9.90, 600, DATE_ADD(NOW(), INTERVAL 2 DAY),  DATE_ADD(NOW(), INTERVAL 50 HOUR), 0),
('SEC014', 22,  29.90, 200, DATE_ADD(NOW(), INTERVAL 2 DAY),  DATE_ADD(NOW(), INTERVAL 50 HOUR), 0),
('SEC015', 36,  29.90, 300, DATE_ADD(NOW(), INTERVAL 2 DAY),  DATE_ADD(NOW(), INTERVAL 50 HOUR), 0),
('SEC016', 45,  35.00, 280, DATE_ADD(NOW(), INTERVAL 3 DAY),  DATE_ADD(NOW(), INTERVAL 74 HOUR), 0),
('SEC017', 49,  59.00, 180, DATE_ADD(NOW(), INTERVAL 3 DAY),  DATE_ADD(NOW(), INTERVAL 74 HOUR), 0);

-- ==========================================
-- 已结束的秒杀（status=2）
-- end_time 已过，库存清空代表已抢光
-- ==========================================
INSERT INTO `seckill_goods` (`seckill_id`, `goods_id`, `seckill_price`, `stock_count`, `start_time`, `end_time`, `status`) VALUES
('SEC018', 7,  199.00, 0, DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 2),
('SEC019', 40,  22.90, 0, DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), 2),
('SEC020', 52, 229.00, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR), 2);
