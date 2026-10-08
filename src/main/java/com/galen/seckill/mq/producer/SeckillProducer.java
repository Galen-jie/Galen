package com.galen.seckill.mq.producer;


import com.galen.seckill.config.RabbitMQConfig;
import com.galen.seckill.mq.message.SeckillMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component

public class SeckillProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    public void send(SeckillMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_EXCHANGE
                , RabbitMQConfig.SECKILL_ROUTING
                , message
        );
    }
}
