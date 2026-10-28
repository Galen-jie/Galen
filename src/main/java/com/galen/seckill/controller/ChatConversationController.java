package com.galen.seckill.controller;

import com.galen.seckill.common.Result;
import com.galen.seckill.dto.ChatMessageRequest;
import com.galen.seckill.service.ChatConversationService;
import com.galen.seckill.service.ChatMessageService;
import com.galen.seckill.vo.ChatConversationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 智能助手会话控制器
 *
 * @author Galen
 * @since 2026-10-26
 */
@Slf4j
@RestController
@RequestMapping("/conversation")
@Tag(name = "智能助手会话", description = "智能助手会话与消息相关接口")
public class ChatConversationController {

    @Autowired
    private ChatConversationService conversationService;

    @Autowired
    private ChatMessageService messageService;

    /**
     * 新增会话（标题为时间戳，默认插入一句招呼语）
     */
    @Operation(summary = "新增会话")
    @PostMapping
    public Result<ChatConversationVO> createConversation() {
        return Result.success(conversationService.createConversation());
    }

    /**
     * 查询当前用户的会话列表
     */
    @Operation(summary = "查询会话列表")
    @GetMapping("/list")
    public Result<List<ChatConversationVO>> listConversations() {
        return Result.success(conversationService.listConversations());
    }

    /**
     * 查询会话详情（含消息列表）
     */
    @Operation(summary = "查询会话详情")
    @GetMapping("/{id}")
    public Result<ChatConversationVO> getConversation(@PathVariable String id) {
        return Result.success(conversationService.getConversation(id));
    }

    /**
     * 删除会话（级联删除其消息）
     */
    @Operation(summary = "删除会话")
    @DeleteMapping("/{id}")
    public Result<Void> deleteConversation(@PathVariable String id) {
        conversationService.deleteConversation(id);
        return Result.success();
    }

    /**
     * 发送消息（SSE 流式返回）
     */
    @Operation(summary = "发送消息（SSE流式）")
    @PostMapping(value = "/{id}/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> sendMessage(@PathVariable String id,
                                                     @Valid @RequestBody ChatMessageRequest request) {
        return messageService.sendMessage(id, request.getContent());
    }
}
