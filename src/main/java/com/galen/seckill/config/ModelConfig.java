package com.galen.seckill.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 智能助手模型配置
 *
 * @author Galen
 * @since 2026-10-26
 */
@Configuration
public class ModelConfig {

    /**
     * 构造一个不带默认 advisor 的 ChatClient，由业务侧手动组装消息上下文
     * （历史记忆通过 {@link MyChatMemory} 加载）。
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }
}
