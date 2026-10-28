package com.galen.seckill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.galen.seckill.common.ResultCode;
import com.galen.seckill.entity.ChatConversation;
import com.galen.seckill.entity.ChatMessage;
import com.galen.seckill.exception.BusinessException;
import com.galen.seckill.mapper.ChatConversationMapper;
import com.galen.seckill.mapper.ChatMessageMapper;
import com.galen.seckill.service.ChatConversationService;
import com.galen.seckill.util.UserHolder;
import com.galen.seckill.vo.ChatConversationVO;
import com.galen.seckill.vo.ChatMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 智能助手会话服务实现类
 *
 * @author Galen
 * @since 2026-10-26
 */
@Slf4j
@Service
public class ChatConversationServiceImpl implements ChatConversationService {

    private static final DateTimeFormatter TITLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String GREETING = "你好呀，我是宠物助手～ 关于流浪动物救助、宠物喂养、健康知识等问题都可以问我，有什么我能帮你的吗？";

    @Autowired
    private ChatConversationMapper conversationMapper;

    @Autowired
    private ChatMessageMapper messageMapper;

    @Override
    public ChatConversationVO createConversation() {
        Long userId = UserHolder.getUser().getId();

        ChatConversation conversation = new ChatConversation();
        conversation.setUserId(userId);
        conversation.setTitle(LocalDateTime.now().format(TITLE_FORMATTER));
        conversationMapper.insert(conversation);

        ChatMessage greeting = new ChatMessage();
        greeting.setConversationId(conversation.getId());
        greeting.setRole("assistant");
        greeting.setContent(GREETING);
        messageMapper.insert(greeting);

        log.info("用户{}创建会话: {}", userId, conversation.getId());

        return convertToVO(conversation);
    }

    @Override
    public List<ChatConversationVO> listConversations() {
        Long userId = UserHolder.getUser().getId();

        List<ChatConversation> conversations = conversationMapper.selectList(
                new LambdaQueryWrapper<ChatConversation>()
                        .eq(ChatConversation::getUserId, userId)
                        .orderByDesc(ChatConversation::getUpdateTime));

        return conversations.stream().map(this::convertToVO).toList();
    }

    @Override
    public ChatConversationVO getConversation(String id) {
        ChatConversation conversation = getOwnedConversation(id);

        List<ChatMessage> messages = messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getConversationId, id)
                        .orderByAsc(ChatMessage::getId));

        ChatConversationVO vo = convertToVO(conversation);
        vo.setMessages(messages.stream().map(this::convertToMessageVO).toList());
        return vo;
    }

    @Override
    public void deleteConversation(String id) {
        ChatConversation conversation = getOwnedConversation(id);

        messageMapper.delete(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, id));
        conversationMapper.deleteById(conversation.getId());

        log.info("用户{}删除会话: {}", conversation.getUserId(), id);
    }

    private ChatConversation getOwnedConversation(String id) {
        Long userId = UserHolder.getUser().getId();
        ChatConversation conversation = conversationMapper.selectById(id);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CONVERSATION_NOT_FOUND);
        }
        return conversation;
    }

    private ChatConversationVO convertToVO(ChatConversation conversation) {
        return BeanUtil.copyProperties(conversation, ChatConversationVO.class);
    }

    private ChatMessageVO convertToMessageVO(ChatMessage message) {
        return BeanUtil.copyProperties(message, ChatMessageVO.class);
    }
}
