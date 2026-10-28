package com.galen.seckill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galen.seckill.common.ResultCode;
import com.galen.seckill.constant.PromptConstant;
import com.galen.seckill.entity.ChatConversation;
import com.galen.seckill.entity.ChatMessage;
import com.galen.seckill.exception.BusinessException;
import com.galen.seckill.mapper.ChatConversationMapper;
import com.galen.seckill.mapper.ChatMessageMapper;
import com.galen.seckill.service.ChatMessageService;
import com.galen.seckill.util.UserHolder;
import com.galen.seckill.vo.ChatMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 智能助手消息服务实现类
 *
 * @author Galen
 * @since 2026-10-26
 */
@Slf4j
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Autowired
    private ChatConversationMapper conversationMapper;

    @Autowired
    private ChatMessageMapper messageMapper;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Flux<ServerSentEvent<String>> sendMessage(String conversationId, String content) {
        Long userId = UserHolder.getUser().getId();

        ChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CONVERSATION_NOT_FOUND);
        }

        // 1. 落库用户消息
        ChatMessage userMessage = new ChatMessage();
        userMessage.setConversationId(conversationId);
        userMessage.setRole("user");
        userMessage.setContent(content);
        userMessage.setCreateTime(LocalDateTime.now());
        messageMapper.insert(userMessage);

        // 2. 组装上下文：系统提示词 + 历史消息（含刚写入的用户消息）
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(PromptConstant.SYSTEM_PROMPT));
        messages.addAll(chatMemory.get(conversationId));

        // 3. 预留助手消息占位，拿到 id 供 done 事件使用
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setConversationId(conversationId);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent("");
        assistantMessage.setCreateTime(LocalDateTime.now());
        messageMapper.insert(assistantMessage);

        StringBuilder fullContent = new StringBuilder();

        Flux<ServerSentEvent<String>> deltaFlux = chatClient.prompt()
                .messages(messages)
                .stream()
                .content()
                .map(delta -> {
                    fullContent.append(delta);
                    return ServerSentEvent.<String>builder()
                            .event("delta")
                            .data(toJson(Map.<String, Object>of("content", delta)))
                            .build();
                });

        Flux<ServerSentEvent<String>> doneFlux = Flux.defer(() -> {
            assistantMessage.setContent(fullContent.toString());
            messageMapper.updateById(assistantMessage);
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("done")
                    .data(toJson(Map.<String, Object>of("message", toVO(assistantMessage))))
                    .build());
        });

        return Flux.concat(deltaFlux, doneFlux)
                .onErrorResume(e -> {
                    log.error("流式对话失败, conversationId={}", conversationId, e);
                    messageMapper.deleteById(assistantMessage.getId());
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data(toJson(Map.<String, Object>of("code", 500, "message", "模型调用失败，请稍后重试")))
                            .build());
                });
    }

    private ChatMessageVO toVO(ChatMessage message) {
        return BeanUtil.copyProperties(message, ChatMessageVO.class);
    }

    private String toJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.warn("SSE数据序列化失败", e);
            return "{}";
        }
    }
}
