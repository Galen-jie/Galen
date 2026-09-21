package com.galen.seckill.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.galen.seckill.constant.GoodsStatus;
import com.galen.seckill.entity.SeckillGoods;
import com.galen.seckill.mapper.SeckillGoodsMapper;
import com.galen.seckill.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 秒杀商品库存预热定时任务
 *
 * 定时扫描即将开始的秒杀商品，提前将数据预热到Redis
 *
 * @author Galen
 * @since 2026-09-21
 */
@Slf4j
@Component
public class SeckillGoodsPreheatTask {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 预热提前时间（分钟）
     * 在秒杀开始前N分钟进行预热
     */
    private static final int PREHEAT_ADVANCE_MINUTES = 5;

    /**
     * 预热时间窗口（分钟）
     * 预热开始前N分钟到开始时刻之间的商品
     */
    private static final int PREHEAT_WINDOW_MINUTES = 6;

    /**
     * 定时预热任务
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void preheatSeckillGoods() {
        log.info("========== 开始执行秒杀商品预热任务 ==========");

        try {
            LocalDateTime now = LocalDateTime.now();

            // 计算预热窗口
            // 查找开始时间在 [当前时间 + 5分钟, 当前时间 + 11分钟] 之间的秒杀商品
            LocalDateTime windowStart = now.plusMinutes(PREHEAT_ADVANCE_MINUTES);
            LocalDateTime windowEnd = now.plusMinutes(PREHEAT_ADVANCE_MINUTES + PREHEAT_WINDOW_MINUTES);

            // 查询即将开始的秒杀商品（状态为0-未开始）
            LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SeckillGoods::getStatus, 0) // 未开始
                   .between(SeckillGoods::getStartTime, windowStart, windowEnd);

            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(wrapper);

            if (seckillGoodsList == null || seckillGoodsList.isEmpty()) {
                log.info("当前没有需要预热的秒杀商品");
                return;
            }

            log.info("查询到 {} 个需要预热的秒杀商品", seckillGoodsList.size());

            int successCount = 0;
            int failCount = 0;

            for (SeckillGoods seckillGoods : seckillGoodsList) {
                try {
                    // 检查是否已经预热过
                    if (redisUtil.existsSeckillGoods(seckillGoods.getSeckillId())) {
                        log.info("秒杀商品已预热，跳过。seckillId: {}", seckillGoods.getSeckillId());
                        continue;
                    }

                    // 预热到Redis
                    redisUtil.preheatSeckillGoods(seckillGoods);

                    // 计算距离开始的剩余时间
                    long minutesUntilStart = ChronoUnit.MINUTES.between(now, seckillGoods.getStartTime());

                    log.info("预热成功 - seckillId: {}, 商品ID: {}, 开始时间: {}, 剩余{}分钟",
                            seckillGoods.getSeckillId(),
                            seckillGoods.getGoodsId(),
                            seckillGoods.getStartTime(),
                            minutesUntilStart);

                    successCount++;
                } catch (Exception e) {
                    log.error("预热失败 - seckillId: {}", seckillGoods.getSeckillId(), e);
                    failCount++;
                }
            }

            log.info("========== 预热任务完成 ========== 成功: {}, 失败: {}", successCount, failCount);

        } catch (Exception e) {
            log.error("执行秒杀商品预热任务失败", e);
        }
    }

    /**
     * 手动触发预热（用于测试或紧急情况）
     * 预热所有未开始和进行中的秒杀商品
     */
    public void manualPreheat() {
        log.info("========== 手动触发秒杀商品预热 ==========");

        try {
            // 查询所有未开始和进行中的秒杀商品
            LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(SeckillGoods::getStatus, 0, 1); // 0-未开始，1-进行中

            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(wrapper);

            if (seckillGoodsList == null || seckillGoodsList.isEmpty()) {
                log.info("没有需要预热的秒杀商品");
                return;
            }

            log.info("查询到 {} 个需要预热的秒杀商品", seckillGoodsList.size());

            for (SeckillGoods seckillGoods : seckillGoodsList) {
                try {
                    redisUtil.preheatSeckillGoods(seckillGoods);
                    log.info("预热成功 - seckillId: {}", seckillGoods.getSeckillId());
                } catch (Exception e) {
                    log.error("预热失败 - seckillId: {}", seckillGoods.getSeckillId(), e);
                }
            }

            log.info("========== 手动预热完成 ==========");

        } catch (Exception e) {
            log.error("手动预热失败", e);
        }
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void updateStatus(){
        try {
            log.info("========== 更新秒杀商品状态 ==========");
            QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<SeckillGoods>();
            queryWrapper.eq("status", 0)
                    .le("start_time", LocalDateTime.now());
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(queryWrapper);
            if(seckillGoodsList==null || seckillGoodsList.isEmpty()){
                return;
            } else{
                for(SeckillGoods seckillGoods : seckillGoodsList){
                    seckillGoods.setStatus(GoodsStatus.START);
                    seckillGoodsMapper.updateById(seckillGoods);
                    redisUtil.updateSeckillGoodsStatus(seckillGoods.getSeckillId(), seckillGoods.getStatus());
                    log.info("更新秒杀商品状态 - seckillId: {}, status: {}", seckillGoods.getSeckillId(), seckillGoods.getStatus());
                }
            }

            queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("status", 1)
                    .le("end_time", LocalDateTime.now());
            seckillGoodsList = seckillGoodsMapper.selectList(queryWrapper);
            if(seckillGoodsList==null||seckillGoodsList.isEmpty()){
                return;
            }else{
                for(SeckillGoods seckillGoods : seckillGoodsList){
                    seckillGoods.setStatus(GoodsStatus.END);
                    seckillGoodsMapper.updateById(seckillGoods);
                    redisUtil.updateSeckillGoodsStatus(seckillGoods.getSeckillId(), seckillGoods.getStatus());
                    log.info("更新秒杀商品状态 - seckillId: {}, status: {}", seckillGoods.getSeckillId(), seckillGoods.getStatus());
                }
            }

        } catch (Exception e) {
            log.error("更新秒杀商品状态失败", e);
        }

    }
}