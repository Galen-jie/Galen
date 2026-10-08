package com.galen.seckill.annotation;

import java.lang.annotation.*;

/**
 * 角色权限注解
 *
 * @author Galen
 * @since 2026-10-08
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 需要的角色数组
     */
    int[] value() default {};
}