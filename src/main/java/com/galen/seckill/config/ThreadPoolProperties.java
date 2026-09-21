package com.galen.seckill.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Data
@Component
public class ThreadPoolProperties {
    @Value("${galen.threadpool.core-size}")
    private int coreSize;
    @Value("${galen.threadpool.max-pool-size}")
    private int maxPoolSize;
    @Value("${galen.threadpool.queue-capacity}")
    private int queueCapacity;
    @Value("${galen.threadpool.keep-alive-time}")
    private int keepAliveTime;
}
