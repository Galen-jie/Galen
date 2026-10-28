package com.galen.seckill.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.galen.seckill.entity.ChatConversation;
import com.galen.seckill.entity.ChatMessage;
import com.galen.seckill.mapper.ChatConversationMapper;
import com.galen.seckill.mapper.ChatMessageMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 MySQL 的会话记忆实现，将多轮对话历史持久化到 chat_message 表。
 *
 * @author Galen
 * @since 2026-10-26
 */
@Component
public class MyChatMemory implements ChatMemory {


    private final ChatMessageMapper chatMessageMapper;

    public MyChatMemory(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
    }


    @Override
    public void add(String conversationId, List<Message> messages) {
        for (Message message : messages) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setConversationId(conversationId);
            chatMessage.setRole(message.getMessageType().name().toLowerCase());
            chatMessage.setContent(message.getText());
            chatMessageMapper.insert(chatMessage);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        //这里只获取最近的20条记录
        List<ChatMessage> rows = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getConversationId, conversationId)
                        .orderByDesc(ChatMessage::getId)
                        .last("limit 20"));
        List<ChatMessage> messages = new java.util.ArrayList<>(rows);
        Collections.reverse(messages);
        return messages.stream().map(this::toMessage).collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        chatMessageMapper.delete(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, conversationId));
    }

    private Message toMessage(ChatMessage e) {
        return switch (e.getRole()) {
            case "assistant" -> new AssistantMessage(e.getContent());
            case "system" -> new SystemMessage(e.getContent());
            default -> new UserMessage(e.getContent());
        };
    }
}
