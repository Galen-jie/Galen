package com.galen.seckill.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 接口文档配置
 *
 * @author Galen
 * @since 2024-01-01
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI galenOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Galen 秒杀系统接口文档")
                        .description("Galen 分布式秒杀系统 API")
                        .version("1.0.0")
                        .contact(new Contact().name("Galen")));
    }
}
