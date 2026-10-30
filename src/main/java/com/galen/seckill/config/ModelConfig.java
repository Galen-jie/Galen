package com.galen.seckill.config;

import com.galen.seckill.tool.AssistantTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 智能助手模型配置
 *
 * @author Galen
 * @since 2026-10-26
 */
@Configuration
public class ModelConfig {

    /**
     * 自动收集所有标注 {@link AssistantTool} 的 Bean 里的 {@code @Tool} 方法，
     * 作为统一的工具提供者。新增工具类只需标注 {@code @AssistantTool}，无需在此注册。
     */
    @Bean
    public ToolCallbackProvider assistantToolProvider(ListableBeanFactory beanFactory) {
        Map<String, Object> toolBeans = beanFactory.getBeansWithAnnotation(AssistantTool.class);
        return ToolCallbackProvider.from(ToolCallbacks.from(toolBeans.values().toArray()));
    }

    /**
     * 构造一个不带默认 advisor 的 ChatClient，由业务侧手动组装消息上下文
     * （历史记忆通过 {@link MyChatMemory} 加载），并统一挂载工具提供者用于 Function Calling。
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel,
                                 ToolCallbackProvider assistantToolProvider) {
        return ChatClient.builder(openAiChatModel)
                .defaultToolCallbacks(assistantToolProvider)
                .build();
    }
}
