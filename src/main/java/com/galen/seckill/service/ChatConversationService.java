package com.galen.seckill.service;

import com.galen.seckill.vo.ChatConversationVO;

import java.util.List;

/**
 * 智能助手会话服务接口
 *
 * @author Galen
 * @since 2026-10-26
 */
public interface ChatConversationService {

    /**
     * 新增会话（标题为时间戳，并默认插入一句招呼语）
     */
    ChatConversationVO createConversation();

    /**
     * 查询当前用户的会话列表
     */
    List<ChatConversationVO> listConversations();

    /**
     * 查询会话详情（含消息列表）
     */
    ChatConversationVO getConversation(String id);

    /**
     * 删除会话（级联删除其消息）
     */
    void deleteConversation(String id);
}
