package com.galen.seckill.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Session配置类
 * 使用Redis存储Session，实现分布式Session
 *
 * @author Galen
 * @since 2024-01-01
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800) // Session过期时间30分钟
public class SessionConfig {
}