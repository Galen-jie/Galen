package com.galen.seckill.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能助手会话VO
 *
 * @author Galen
 * @since 2026-10-26
 */
@Data
public class ChatConversationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String title;

    private List<ChatMessageVO> messages;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
