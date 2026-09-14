package com.galen.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Galen分布式秒杀系统启动类
 *
 * @author Galen
 * @since 2024-01-01
 */
@SpringBootApplication
@MapperScan("com.galen.seckill.mapper")
@EnableScheduling
@EnableAsync
public class SeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class, args);
        System.out.println("========================================");
        System.out.println("   Galen秒杀系统启动成功！");
        System.out.println("   访问地址: http://localhost:8080");
        System.out.println("   Druid监控: http://localhost:8080/druid");
        System.out.println("========================================");
    }

}