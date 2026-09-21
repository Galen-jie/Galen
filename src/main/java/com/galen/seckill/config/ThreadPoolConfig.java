package com.galen.seckill.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * @author Galen
 * @since 2024-01-01
 */
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class ThreadPoolConfig {

    private final ThreadPoolProperties threadPoolProperties;
    /**
     * 异步任务线程池
     */
    @Bean("asyncTaskExecutor")
    public AsyncTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(threadPoolProperties.getCoreSize());
        // 最大线程数
        executor.setMaxPoolSize(threadPoolProperties.getMaxPoolSize());
        // 队列容量
        executor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
        // 线程名称前缀
        executor.setThreadNamePrefix("Async-Task-");
        // 拒绝策略：当任务队列满时，由调用线程处理该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 线程存活时间
        executor.setKeepAliveSeconds(threadPoolProperties.getKeepAliveTime());
        executor.initialize();
        return executor;
    }

    /**
     * 秒杀专用线程池
     */
    @Bean("seckillTaskExecutor")
    public AsyncTaskExecutor seckillTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolProperties.getCoreSize());
        executor.setMaxPoolSize(threadPoolProperties.getMaxPoolSize());
        executor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
        executor.setThreadNamePrefix("Seckill-Task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(threadPoolProperties.getKeepAliveTime());
        executor.initialize();
        return executor;
    }
}