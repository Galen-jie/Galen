package com.galen.seckill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 评论DTO
 *
 * @author Galen
 * @since 2026-10-08
 */
@Data
public class PetCommentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 宠物ID
     */
    @NotNull(message = "宠物ID不能为空")
    private Long petId;

    /**
     * 父评论ID（0表示一级评论）
     */
    private Long parentId = 0L;

    /**
     * 回复用户ID
     */
    private Long replyUserId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;
}