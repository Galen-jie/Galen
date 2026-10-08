package com.galen.seckill.util;


import com.galen.seckill.entity.SeckillGoods;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class StockRedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final static String STOCK_KEY = "galen:stock:";

    public static DefaultRedisScript<Long> DECREASE_STOCK_SCRIPT;

    public static DefaultRedisScript<Long> INCREASE_STOCK_SCRIPT;

    static{
        DECREASE_STOCK_SCRIPT = new DefaultRedisScript<Long>();
        DECREASE_STOCK_SCRIPT.setScriptSource(
                new ResourceScriptSource(
                        new ClassPathResource("decrease_stock.lua"))
        );
        INCREASE_STOCK_SCRIPT = new DefaultRedisScript<Long>();
        INCREASE_STOCK_SCRIPT.setScriptSource(
                new ResourceScriptSource(
                        new ClassPathResource("rollback_stock.lua"))
        );
        log.info("初始化脚本成功");
    }

    public void preHeatStock(SeckillGoods seckillGoods) {
        try {
            String key=getStockKey(seckillGoods.getGoodsId());
            redisTemplate.opsForValue().set(key, seckillGoods.getStockCount());
            log.info("预热库存成功，seckillId: {}", seckillGoods.getSeckillId());
        } catch (Exception e) {
            log.error("预热库存失败，seckillId: {}", seckillGoods.getSeckillId(), e);
        }
    }
    public boolean decreaseStock(Long Id, Integer count) {
        String key=getStockKey(Id);
        try {
            Long res = redisTemplate.execute(
                    DECREASE_STOCK_SCRIPT,
                    Collections.singletonList(key),
                    String.valueOf(count)
            );
            if(res==null||res==-1){
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("库存不足，seckillId: {}, count: {}", Id, count, e);
            return false;
        }
    }
    private String getStockKey(Long productId) {
        return STOCK_KEY + productId;
    }
    public void rollbackStock(Long id, Integer count){
        try {
            String key=getStockKey(id);
            redisTemplate.execute(
                    INCREASE_STOCK_SCRIPT,
                    Collections.singletonList(key),
                    String.valueOf(count)
            );
            log.info("库存回滚成功，seckillId: {}, count: {}", id, count);
        } catch (Exception e) {
            log.error("库存回滚失败，seckillId: {}, count: {}", id, count, e);
        }
    }

}
