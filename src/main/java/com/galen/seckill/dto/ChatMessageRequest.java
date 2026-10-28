package com.galen.seckill.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 智能助手发送消息请求
 *
 * @author Galen
 * @since 2026-10-26
 */
@Data
public class ChatMessageRequest {

    @NotBlank(message = "消息内容不能为空")
    private String content;
}
