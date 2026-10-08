package com.galen.seckill.service;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.dto.PetCommentDTO;
import com.galen.seckill.vo.PetCommentVO;

/**
 * 宠物评论服务接口
 *
 * @author Galen
 * @since 2026-10-08
 */
public interface PetCommentService {

    /**
     * 发表评论
     *
     * @param commentDTO 评论DTO
     * @return 评论VO
     */
    PetCommentVO createComment(PetCommentDTO commentDTO);

    /**
     * 删除评论
     *
     * @param id 评论ID
     */
    void deleteComment(Long id);

    /**
     * 获取评论列表
     *
     * @param petId 宠物ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<PetCommentVO> getCommentList(Long petId, int pageNum, int pageSize);

    /**
     * 点赞评论
     *
     * @param id 评论ID
     */
    void likeComment(Long id);

    /**
     * 取消点赞评论
     *
     * @param id 评论ID
     */
    void unlikeComment(Long id);
}