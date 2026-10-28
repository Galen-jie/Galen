package com.galen.seckill.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 智能助手消息VO
 *
 * @author Galen
 * @since 2026-10-26
 */
@Data
public class ChatMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String role;

    private String content;

    private LocalDateTime createTime;
}
