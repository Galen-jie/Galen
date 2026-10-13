package com.galen.seckill.mq.consumer;

import com.galen.seckill.config.RabbitMQConfig;
import com.galen.seckill.entity.SeckillGoods;
import com.galen.seckill.entity.SeckillOrder;
import com.galen.seckill.mapper.SeckillGoodsMapper;
import com.galen.seckill.mapper.SeckillOrderMapper;
import com.galen.seckill.mq.message.SeckillMessage;
import com.galen.seckill.util.StockRedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class SeckillComsumer {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private StockRedisUtil stockRedisUtil;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    public void receive(SeckillMessage message) {
        String orderNo = message.getOrderNo();
        log.info("接收到秒杀订单消息：{}", orderNo);

        // 幂等：订单号已存在则跳过（MQ 可能重复投递）
        if (seckillOrderMapper.selectByOrderNo(orderNo) != null) {
            log.warn("订单已存在，跳过重复消息：{}", orderNo);
            return;
        }
        // 判重：用户已购买过该秒杀商品
        if (seckillOrderMapper.selectByUserIdAndSeckillId(
                message.getUserId(), message.getSeckillId(), message.getGoodsId()) != null) {
            log.warn("用户已购买过该商品，跳过：{}", orderNo);
            return;
        }

        // 查秒杀商品（拿最新 version 做乐观锁）
        SeckillGoods seckillGoods = seckillGoodsMapper.selectBySeckillId(message.getSeckillId());
        if (seckillGoods == null) {
            stockRedisUtil.rollbackStock(message.getSeckillId(), 1);
            log.error("秒杀商品不存在，回滚 Redis 库存：{}", orderNo);
            return;
        }

        // 时间窗口校验
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(seckillGoods.getStartTime()) || now.isAfter(seckillGoods.getEndTime())) {
            stockRedisUtil.rollbackStock(message.getSeckillId(), 1);
            log.warn("秒杀不在进行中，回滚 Redis 库存：{}", orderNo);
            return;
        }

        // 第二道防线：DB 乐观锁扣库存（带重试）
        int result = 0;
        for (int i = 0; i < 3; i++) {
            result = seckillGoodsMapper.decreaseStockWithVersion(seckillGoods.getId(), 1, seckillGoods.getVersion());
            if (result > 0) {
                break;
            }
            // 失败后重新查，区分「没货」还是「version 冲突」
            seckillGoods = seckillGoodsMapper.selectBySeckillId(message.getSeckillId());
            if (seckillGoods.getStockCount() <= 0) {
                stockRedisUtil.rollbackStock(message.getSeckillId(), 1);
                log.warn("库存已售罄，回滚 Redis 库存：{}", orderNo);
                return;
            }
        }
        if (result <= 0) {
            // DB 扣减未成功，回滚 Redis 库存（此时 DB 还没扣，回滚安全）
            stockRedisUtil.rollbackStock(message.getSeckillId(), 1);
            log.warn("扣减库存失败，回滚 Redis 库存：{}", orderNo);
            return;
        }

        // 建完整订单（DB 已扣减成功，这里失败不再回滚 Redis，避免超卖）
        try {
            SeckillOrder order = new SeckillOrder();
            order.setOrderNo(orderNo);
            order.setUserId(message.getUserId());
            order.setSeckillId(message.getSeckillId());
            order.setGoodsId(message.getGoodsId());
            order.setSeckillGoodsId(message.getSeckillGoodsId());
            order.setGoodsName(message.getGoodsName());
            order.setGoodsImg(message.getGoodsImg());
            order.setOrderPrice(message.getOrderPrice());
            order.setStatus(0);
            order.setReceiverName(message.getReceiverName());
            order.setReceiverPhone(message.getReceiverPhone());
            order.setReceiverAddress(message.getReceiverAddress());
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            seckillOrderMapper.insert(order);
            log.info("秒杀订单创建成功：{}", orderNo);
        } catch (Exception e) {
            log.error("订单创建失败：{}", orderNo, e);
        }
    }
}
