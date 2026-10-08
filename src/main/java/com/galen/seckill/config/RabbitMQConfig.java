package com.galen.seckill.config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String SECKILL_QUEUE = "seckill_queue";
    public static final String SECKILL_EXCHANGE = "seckill_exchange";
    public static final String SECKILL_ROUTING = "seckill_routing";
    public static final String DEAD_QUEUE = "seckill_dlx_queue";
    public static final String DEAD_EXCHANGE = "seckill_dlx_exchange";

    @Bean
    public Queue seckillQueue() {
        return QueueBuilder.durable(SECKILL_QUEUE)
                .deadLetterExchange(DEAD_EXCHANGE)
                .build();
    }
    @Bean
    public DirectExchange seckillExchange() {
        return ExchangeBuilder.directExchange(SECKILL_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding seckillBinding() {
        return BindingBuilder.bind(seckillQueue()).to(seckillExchange()).with(SECKILL_ROUTING);
    }
    @Bean
    public Queue deadQueue() {
        return QueueBuilder.durable(DEAD_QUEUE).build();
    }
    @Bean
    public DirectExchange deadExchange() {
        return ExchangeBuilder.directExchange(DEAD_EXCHANGE).durable(true).build();
    }
    @Bean
    public Binding deadBinding() {
        return BindingBuilder.bind(deadQueue()).to(deadExchange()).with("dead_routing");
    }
}
