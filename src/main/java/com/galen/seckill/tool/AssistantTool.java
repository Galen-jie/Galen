package com.galen.seckill.tool;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记智能助手的工具类，供 {@code ToolCallbackProvider} 自动收集注册。
 * 标注此注解的类里带 {@code @Tool} 的方法会被 Spring AI 暴露为 Function Calling 工具。
 *
 * @author Galen
 * @since 2026-10-29
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface AssistantTool {
}
