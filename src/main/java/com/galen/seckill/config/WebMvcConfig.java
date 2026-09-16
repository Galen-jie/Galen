package com.galen.seckill.config;

import com.galen.seckill.interceptor.LoginInterceptor;
import com.galen.seckill.interceptor.RefreshInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 *
 * @author Galen
 * @since 2024-01-01
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;
    @Autowired
    private RefreshInterceptor refreshInterceptor;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .order(2)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/user/logout",
                        "/druid/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/v2/api-docs",
                        "/webjars/**",
                        "/actuator/**",
                        "/error"
                );
        registry.addInterceptor(refreshInterceptor)
                .order(1)
                .addPathPatterns("/**");
    }

    /**
     * 静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}