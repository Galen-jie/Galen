-- ==========================================
-- 宠物商城商品数据
-- Author: Galen
-- Date: 2026-11-01
-- 说明：
--   1. 保留原有分类（1-数码产品 / 2-手机 / 3-电脑 / 4-服装）不动
--   2. 新增子分类 (id=6~14)，父分类为 5（宠物用品，需已存在于库中）
--   3. goods_img 使用 OSS 图片 URL，{id} 为商品主键
--   4. goods 采用显式 id（从 5 开始，避开已有 4 条种子商品），请确保库里 goods 当前最大 id 小于 5
-- ==========================================

USE `galen_seckill`;

-- ==========================================
-- 新增宠物用品分类
-- ==========================================
INSERT INTO `goods_category` (`id`, `category_name`, `parent_id`, `sort`) VALUES
(6,  '宠物食品', 5, 1),
(7,  '宠物零食', 5, 2),
(8,  '宠物玩具', 5, 3),
(9,  '宠物服饰', 5, 4),
(10, '宠物训练', 5, 5),
(11, '洗护清洁', 5, 6),
(12, '宠物出行', 5, 7),
(13, '健康保健', 5, 8),
(14, '家居窝垫', 5, 9);

-- ==========================================
-- 宠物食品 (category_id = 6)
-- ==========================================
INSERT INTO `goods` (`id`, `goods_name`, `goods_title`, `goods_img`, `goods_detail`, `goods_price`, `stock`, `category_id`, `status`) VALUES
(5,  '皇家猫粮 成猫粮 4kg', '皇家 F32 室内成猫全价猫粮 4kg 营养均衡', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/5.jpg', '专为室内成猫设计，优质蛋白易消化，帮助维持理想体重与泌尿系统健康。', 329.00, 500, 6, 1),
(6,  '渴望六种鱼 无谷猫粮 5.4kg', 'Orijen 渴望 六种鱼无谷天然猫粮 5.4kg 高蛋白', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/6.jpg', '六种深海鱼配方，85%动物蛋白，无谷物添加，满足猫咪肉食天性。', 569.00, 300, 6, 1),
(7,  '皇家狗粮 小型犬成犬粮 3kg', '皇家 MINI 小型成犬粮 3kg 呵护口腔', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/7.jpg', '针对小型犬设计，颗粒大小适中，富含钙磷促进骨骼健康。', 269.00, 400, 6, 1),
(8,  '比瑞吉 幼犬粮 全犬种 2.5kg', '比瑞吉 全价幼犬粮 2.5kg 奶糕配方 营养成长', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/8.jpg', '含奶糕成分与DHA，助力幼犬脑部发育与骨骼成长。', 139.00, 600, 6, 1),
(9,  '麦富迪 全价猫粮 10kg', '麦富迪 全价成猫粮 10kg 大包装实惠', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/9.jpg', '大容量家庭装，含冻干颗粒，适口性好，性价比之选。', 299.00, 200, 6, 1),
(10, '冠能 幼猫粮 全价营养 2kg', '冠能 幼猫全价粮 2kg 支持免疫力', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/10.jpg', '添加牛初乳与DHA，支持幼猫免疫系统与脑部发育。', 199.00, 350, 6, 1);

-- ==========================================
-- 宠物零食 (category_id = 7)
-- ==========================================
INSERT INTO `goods` (`id`, `goods_name`, `goods_title`, `goods_img`, `goods_detail`, `goods_price`, `stock`, `category_id`, `status`) VALUES
(11, '猫条 鸡肉味 30支装', '派得 猫条 鸡肉味 30支 补水营养湿粮', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/11.jpg', '流质湿粮猫条，独立小包装，补水又营养，猫猫都爱吃。', 39.90, 800, 7, 1),
(12, '冻干鸡肉粒 500g', '麦富迪 冻干鸡肉粒 猫狗通用 500g 纯肉无添加', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/12.jpg', '100%纯鸡肉冻干，锁鲜工艺，猫狗通用，训练奖励佳品。', 69.00, 500, 7, 1),
(13, '洁齿骨 磨牙棒 12支', '宝路 洁齿骨 磨牙棒 12支 清新口气', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/13.jpg', '咀嚼清洁牙齿，减少牙垢，清新口气，狗狗日常磨牙必备。', 29.90, 700, 7, 1),
(14, '猫薄荷 木天蓼 组合装', '猫咪专用 猫薄荷+木天蓼 组合 逗猫放松', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/14.jpg', '天然猫薄荷与木天蓼，帮助猫咪放松减压，增进互动。', 19.90, 600, 7, 1),
(15, '狗狗牛肉粒 400g', '顽皮 狗狗牛肉粒 400g 高蛋白训练零食', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/15.jpg', '精选牛肉制作，软硬适中，高蛋白，训练奖励好选择。', 45.00, 450, 7, 1),
(16, '猫罐头 金枪鱼味 12罐', '伟嘉 猫罐头 金枪鱼味 12罐 湿粮补水', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/16.jpg', '金枪鱼白肉配方，汤汁浓郁，补水易消化。', 59.90, 550, 7, 1);

-- ==========================================
-- 宠物玩具 (category_id = 8)
-- ==========================================
INSERT INTO `goods` (`id`, `goods_name`, `goods_title`, `goods_img`, `goods_detail`, `goods_price`, `stock`, `category_id`, `status`) VALUES
(17, '逗猫棒 羽毛玩具 2支', '不锈钢杆 羽毛铃铛 逗猫棒 2支装', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/17.jpg', '轻巧耐用，羽毛+铃铛组合，激发猫咪捕猎本能。', 15.90, 900, 8, 1),
(18, '猫抓板 瓦楞纸', '加厚瓦楞纸 猫抓板 磨爪护家具', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/18.jpg', '高密度瓦楞纸，耐抓耐磨，保护家具，猫咪磨爪首选。', 25.90, 400, 8, 1),
(19, '狗咬胶 耐咬磨牙玩具', '耐咬狗咬胶 磨牙洁齿 中大型犬适用', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/19.jpg', '环保橡胶材质，耐咬耐摔，帮助磨牙洁齿。', 29.90, 500, 8, 1),
(20, '宠物球 橡胶发声球', '橡胶发声球 宠物玩具 中大型犬', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/20.jpg', '内置发声器，弹性十足，耐咬耐玩，狗狗互动好帮手。', 19.90, 600, 8, 1),
(21, '自动逗猫激光笔', 'USB充电 自动逗猫 激光笔 随机轨迹', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/21.jpg', '自动随机运动光点，无人看管也能逗猫，节能充电设计。', 59.90, 250, 8, 1),
(22, '猫隧道 折叠玩具', '折叠猫隧道 带响纸 钻洞玩耍', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/22.jpg', '可折叠收纳，内置响纸，猫咪钻洞躲猫猫乐趣无穷。', 45.90, 300, 8, 1),
(23, '狗飞盘 耐咬飞盘', '专业狗飞盘 耐咬材质 户外互动', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/23.jpg', '软胶材质不伤牙，飞行稳定，户外遛狗互动首选。', 35.90, 350, 8, 1),
(24, '益智漏食球', '宠物漏食球 益智玩具 慢食互动', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/24.jpg', '可填充零食，滚动漏食，锻炼智力，缓解分离焦虑。', 39.90, 280, 8, 1);

-- ==========================================
-- 宠物服饰 (category_id = 9)
-- ==========================================
INSERT INTO `goods` (`id`, `goods_name`, `goods_title`, `goods_img`, `goods_detail`, `goods_price`, `stock`, `category_id`, `status`) VALUES
(25, '狗狗冬季棉服 保暖', '狗狗加厚棉服 冬季保暖 多尺码可选', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/25.jpg', '加厚摇粒绒内里，防寒保暖，反光条设计夜间更安全。', 69.00, 300, 9, 1),
(26, '宠物雨衣 防水', '宠物防水雨衣 带帽檐 雨天出行', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/26.jpg', '防水面料，可调节尺寸，雨天遛狗不再湿身。', 49.00, 280, 9, 1),
(27, '猫咪项圈 铃铛', '猫咪项圈 铃铛 可调节 安全扣', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/27.jpg', '柔软材质，安全扣设计，铃铛清脆，时尚又醒目。', 16.90, 700, 9, 1),
(28, '宠物牵引绳 胸背带', '宠物胸背带牵引绳 防爆冲 透气舒适', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/28.jpg', '透气网面胸背带，防勒防爆冲，遛狗更轻松。', 59.00, 320, 9, 1),
(29, '狗狗毛衣 针织', '狗狗针织毛衣 保暖 秋冬新款', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/29.jpg', '柔软针织面料，弹性好，保暖舒适，多色可选。', 39.00, 260, 9, 1),
(30, '宠物鞋 防滑', '宠物防滑鞋 4只装 户外防护', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/30.jpg', '防滑鞋底，保护脚垫，户外出行更安心。', 45.00, 220, 9, 1);

-- ==========================================
-- 宠物训练 (category_id = 10)
-- ==========================================
INSERT INTO `goods` (`id`, `goods_name`, `goods_title`, `goods_img`, `goods_detail`, `goods_price`, `stock`, `category_id`, `status`) VALUES
(31, '训练响片', '宠物训练响片 正强化训练 附指导手册', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/31.jpg', '经典正强化训练工具，声音清脆，附训练指导手册。', 12.90, 800, 10, 1),
(32, '宠物尿垫 100片', '宠物尿垫 加厚吸水 100片装 训练必备', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/32.jpg', '高分子吸水层，锁水防漏，训练定点如厕好帮手。', 49.90, 500, 10, 1),
(33, '狗厕所 训练', '狗狗室内厕所 带尿垫卡槽 防滑', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/33.jpg', '防滑底座，可固定尿垫，帮助狗狗养成定点如厕习惯。', 69.00, 200, 10, 1),
(34, '宠物诱导剂 训练喷雾', '宠物如厕诱导剂 定点排便 训练喷雾', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/34.jpg', '天然成分诱导剂，引导宠物在指定位置排便，训练更高效。', 29.90, 350, 10, 1),
(35, '训练零食袋', '宠物训练零食袋 磁吸开口 便携', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/35.jpg', '磁吸快开设计，方便取放零食，训练随取随用。', 22.90, 400, 10, 1);

-- ==========================================
-- 洗护清洁 (category_id = 11)
-- ==========================================
INSERT INTO `goods` (`id`, `goods_name`, `goods_title`, `goods_img`, `goods_detail`, `goods_price`, `stock`, `category_id`, `status`) VALUES
(36, '宠物香波 沐浴露 500ml', '宠物专用香波 温和低敏 留香 500ml', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/36.jpg', '温和无泪配方，深层清洁，持久留香，猫狗通用。', 39.90, 400, 11, 1),
(37, '宠物梳子 除毛刷', '宠物针梳 去浮毛 顺毛不打结', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/37.jpg', '不锈钢针梳，轻松去除浮毛，防止打结，猫咪狗狗通用。', 25.90, 500, 11, 1),
(38, '宠物指甲剪', '宠物指甲剪 带安全挡 防剪伤', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/38.jpg', '锋利耐用，安全挡板设计，防止误剪血线。', 18.90, 600, 11, 1),
(39, '宠物湿巾 80抽', '宠物专用湿巾 去污除菌 80抽', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/39.jpg', '无酒精无刺激，擦拭眼周、爪子、毛发，日常清洁方便。', 15.90, 700, 11, 1),
(40, '除臭喷雾', '宠物除臭喷雾 分解异味 环境清新', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/40.jpg', '生物酶分解异味源，非掩盖式除臭，家居环境更清新。', 32.90, 300, 11, 1);

-- ==========================================
-- 宠物出行 (category_id = 12)
-- ==========================================
INSERT INTO `goods` (`id`, `goods_name`, `goods_title`, `goods_img`, `goods_detail`, `goods_price`, `stock`, `category_id`, `status`) VALUES
(41, '宠物航空箱', '宠物航空箱 托运便携 通风透气', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/41.jpg', '符合航空托运标准，通风透气，出行托运安全可靠。', 159.00, 150, 12, 1),
(42, '宠物背包 太空舱', '透明太空舱宠物背包 透气 外出携带', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/42.jpg', '透明太空舱设计，透气网面，让爱宠外出看风景。', 129.00, 180, 12, 1),
(43, '车载宠物垫', '车载宠物垫 防水防抓 后排保护', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/43.jpg', '防水防抓面料，保护车座，宠物乘车更安心。', 89.00, 200, 12, 1),
(44, '折叠宠物笼', '折叠宠物笼 便携 多尺寸 出行居家两用', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/44.jpg', '可折叠收纳，便携设计，出行居家皆适用。', 199.00, 120, 12, 1);

-- ==========================================
-- 健康保健 (category_id = 13)
-- ==========================================
INSERT INTO `goods` (`id`, `goods_name`, `goods_title`, `goods_img`, `goods_detail`, `goods_price`, `stock`, `category_id`, `status`) VALUES
(45, '宠物益生菌', '宠物益生菌 调理肠胃 改善软便', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/45.jpg', '活性益生菌配方，调理肠道菌群，改善软便腹泻。', 49.00, 350, 13, 1),
(46, '宠物化毛膏', '猫咪化毛膏 排毛球 呵护肠胃', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/46.jpg', '帮助排出体内毛球，缓解毛球症，呵护肠胃健康。', 45.00, 400, 13, 1),
(47, '宠物钙片', '宠物钙片 补钙 强健骨骼 幼宠成宠', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/47.jpg', '易吸收钙源，促进骨骼发育，幼宠成宠皆适用。', 39.00, 450, 13, 1),
(48, '驱虫药 体内外同驱', '宠物驱虫药 体内外同驱 安全有效', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/48.jpg', '体内外寄生虫同驱，药效温和，定期驱虫守护健康。', 89.00, 300, 13, 1);

-- ==========================================
-- 家居窝垫 (category_id = 14)
-- ==========================================
INSERT INTO `goods` (`id`, `goods_name`, `goods_title`, `goods_img`, `goods_detail`, `goods_price`, `stock`, `category_id`, `status`) VALUES
(49, '猫窝 四季通用', '猫咪四季通用窝 柔软舒适 可拆洗', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/49.jpg', '柔软亲肤面料，四季适用，可拆卸清洗，猫咪安睡之选。', 79.00, 250, 14, 1),
(50, '狗窝 保暖', '狗狗保暖窝 加厚 可拆洗 多尺寸', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/50.jpg', '加厚保暖内垫，柔软舒适，可拆洗设计，狗狗温暖小窝。', 99.00, 220, 14, 1),
(51, '宠物垫 防水', '宠物防水垫 防潮防脏 四季通用', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/51.jpg', '底部防水防滑，防潮防脏，笼垫窝垫皆可用。', 49.00, 300, 14, 1),
(52, '猫爬架 多层', '猫咪多层猫爬架 带窝带抓柱 大型', 'https://galen-petrescue.oss-cn-beijing.aliyuncs.com/2026/11/1/52.jpg', '多层结构，剑麻抓柱+舒适猫窝，满足猫咪攀爬抓挠天性。', 299.00, 100, 14, 1);
