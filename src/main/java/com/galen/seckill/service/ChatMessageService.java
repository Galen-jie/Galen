package com.galen.seckill.service;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * 智能助手消息服务接口
 *
 * @author Galen
 * @since 2026-10-26
 */
public interface ChatMessageService {

    /**
     * 发送消息并以 SSE 流式返回助手回复
     *
     * @param conversationId 会话ID
     * @param content        用户消息内容
     * @return SSE 事件流（delta / done / error）
     */
    Flux<ServerSentEvent<String>> sendMessage(String conversationId, String content);
}
