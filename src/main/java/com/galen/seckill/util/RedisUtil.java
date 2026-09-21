package com.galen.seckill.util;

import cn.hutool.core.bean.BeanUtil;
import com.galen.seckill.entity.SeckillGoods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品Redis操作工具类
 * 使用Hash存储秒杀商品信息
 * 支持延迟双删机制保证缓存一致性
 *
 * @author Galen
 * @since 2026-09-21
 */
@Slf4j
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis Key前缀
     */
    private static final String SECKILL_GOODS_KEY_PREFIX = "galen:seckill:goods:";

    /**
     * 延迟双删的延迟时间（毫秒）
     */
    private static final long DELETE_DELAY_MS = 500;

    /**
     * 缓存过期时间（小时）
     */
    private static final long CACHE_EXPIRE_HOURS = 2;

    /**
     * 预热秒杀商品到Redis
     * 将商品完整信息存入Hash
     *
     * @param seckillGoods 秒杀商品信息
     */
    public void preheatSeckillGoods(SeckillGoods seckillGoods) {
        if (seckillGoods == null) {
            log.warn("预热秒杀商品失败，商品信息为空");
            return;
        }

        String key = getSeckillGoodsKey(seckillGoods.getSeckillId());

        try {
            // 将对象转换为Map
            Map<String, Object> goodsMap = BeanUtil.beanToMap(seckillGoods);

            // 存入Redis Hash
            redisTemplate.opsForHash().putAll(key, goodsMap);

            // 设置过期时间（2小时）
            redisTemplate.expire(key, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

            log.info("预热秒杀商品成功，seckillId: {}, key: {}", seckillGoods.getSeckillId(), key);
        } catch (Exception e) {
            log.error("预热秒杀商品失败，seckillId: {}", seckillGoods.getSeckillId(), e);
        }
    }

    /**
     * 从Redis获取秒杀商品信息
     *
     * @param seckillId 秒杀ID
     * @return 秒杀商品信息，不存在返回null
     */
    public SeckillGoods getSeckillGoodsFromRedis(String seckillId) {
        String key = getSeckillGoodsKey(seckillId);

        try {
            Map<Object, Object> goodsMap = redisTemplate.opsForHash().entries(key);

            if (goodsMap == null || goodsMap.isEmpty()) {
                return null;
            }

            // 将Map转换为对象
            return BeanUtil.mapToBean(goodsMap, SeckillGoods.class, true);
        } catch (Exception e) {
            log.error("获取秒杀商品信息失败，seckillId: {}", seckillId, e);
            return null;
        }
    }

    /**
     * 删除秒杀商品缓存
     *
     * @param seckillId 秒杀ID
     */
    public void deleteSeckillGoodsCache(String seckillId) {
        String key = getSeckillGoodsKey(seckillId);

        try {
            Boolean deleted = redisTemplate.delete(key);
            log.info("删除秒杀商品缓存，seckillId: {}, 结果: {}", seckillId, deleted);
        } catch (Exception e) {
            log.error("删除秒杀商品缓存失败，seckillId: {}", seckillId, e);
        }
    }

    /**
     * 延迟双删机制
     * 先删除缓存 -> 延迟500ms -> 再次删除缓存
     *
     * @param seckillId 秒杀ID
     */
    public void deleteSeckillGoodsCacheWithDoubleDeletion(String seckillId) {
        // 第一次删除
        deleteSeckillGoodsCache(seckillId);

        // 延迟后再次删除
        try {
            Thread.sleep(DELETE_DELAY_MS);
            deleteSeckillGoodsCache(seckillId);
            log.info("延迟双删完成，seckillId: {}", seckillId);
        } catch (InterruptedException e) {
            log.error("延迟双删失败，seckillId: {}", seckillId, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 异步延迟双删（推荐使用）
     * 使用独立线程执行延迟双删，不阻塞主线程
     *
     * @param seckillId 秒杀ID
     */
    public void deleteSeckillGoodsCacheAsync(String seckillId) {
        new Thread(() -> {
            try {
                // 第一次删除
                deleteSeckillGoodsCache(seckillId);

                // 延迟
                Thread.sleep(DELETE_DELAY_MS);

                // 第二次删除
                deleteSeckillGoodsCache(seckillId);

                log.info("异步延迟双删完成，seckillId: {}", seckillId);
            } catch (InterruptedException e) {
                log.error("异步延迟双删失败，seckillId: {}", seckillId, e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * 检查秒杀商品缓存是否存在
     *
     * @param seckillId 秒杀ID
     * @return 是否存在
     */
    public boolean existsSeckillGoods(String seckillId) {
        String key = getSeckillGoodsKey(seckillId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 获取Redis Key
     *
     * @param seckillId 秒杀ID
     * @return Redis Key
     */
    private String getSeckillGoodsKey(String seckillId) {
        return SECKILL_GOODS_KEY_PREFIX + seckillId;
    }
}