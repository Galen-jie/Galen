package com.galen.seckill.mq.consumer;


import com.galen.seckill.config.RabbitMQConfig;
import com.galen.seckill.entity.SeckillGoods;
import com.galen.seckill.entity.SeckillOrder;
import com.galen.seckill.mapper.SeckillGoodsMapper;
import com.galen.seckill.mapper.SeckillOrderMapper;
import com.galen.seckill.mq.message.SeckillMessage;
import com.galen.seckill.util.RedisUtil;
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
    //监听队列
    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    public void receive(SeckillMessage message) {
        String orderNo = message.getOrderNo();
        System.out.println("接收到订单号：" + orderNo);
        SeckillOrder seckillOrder = seckillOrderMapper.selectByOrderNo(orderNo);
        if(seckillOrder != null){
            log.warn("订单号已存在，无需重复处理：" + orderNo);
            return;
        }
        SeckillOrder existOrder = seckillOrderMapper.selectByUserIdAndSeckillId(message.getUserId(), message.getSeckillId(), message.getGoodsId());
        if(existOrder != null){
            log.warn("用户已购买过该商品，无需重复处理：" + orderNo);
            return;
        }
        SeckillGoods seckillGoods = seckillGoodsMapper.selectBySeckillId(message.getSeckillId());
        LocalDateTime now = LocalDateTime.now();
        if(now.isBefore(seckillGoods.getStartTime())) {
            log.warn("秒杀未开始：" + orderNo);
            return;
        }
        if(now.isAfter(seckillGoods.getEndTime())) {
            log.warn("秒杀已结束：" + orderNo);
            return;
        }
        int result = 0;
        for(int i=0;i<3;i++){
            result = seckillGoodsMapper.decreaseStockWithVersion(seckillGoods.getId(), 1, seckillGoods.getVersion());
            if(result > 0){
                break;
            }
            seckillGoods = seckillGoodsMapper.selectBySeckillId(message.getSeckillId());
            if(seckillGoods.getStockCount() <= 0){
                log.warn("库存不足：" + orderNo);
                return;
            }
        }
        if(result <= 0){
            log.warn("抱歉，你并没抢到该商品：" + orderNo);
            return;
        }

        SeckillOrder new_Order = new SeckillOrder();
        new_Order.setOrderNo(orderNo);
        new_Order.setUserId(message.getUserId());
        new_Order.setSeckillId(message.getSeckillId());
        new_Order.setGoodsId(seckillGoods.getGoodsId());
        new_Order.setSeckillGoodsId(seckillGoods.getId());
        seckillOrderMapper.insert(new_Order);
    }
}
