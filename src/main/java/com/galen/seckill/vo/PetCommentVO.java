package com.galen.seckill.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 宠物评论VO
 *
 * @author Galen
 * @since 2026-10-08
 */
@Data
public class PetCommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long petId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long parentId;
    private Long replyUserId;
    private String replyUserName;
    private String content;
    private Integer likeCount;
    private Boolean isLiked;
    private LocalDateTime createTime;

    /**
     * 子评论列表
     */
    private List<PetCommentVO> children;
}