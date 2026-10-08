package com.galen.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.galen.seckill.entity.PetComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 宠物评论Mapper接口
 *
 * @author Galen
 * @since 2026-10-08
 */
@Mapper
public interface PetCommentMapper extends BaseMapper<PetComment> {

    /**
     * 点赞数+1
     */
    @Update("UPDATE pet_comment SET like_count = like_count + 1 WHERE id = #{commentId}")
    int incrementLikeCount(Long commentId);

    /**
     * 点赞数-1
     */
    @Update("UPDATE pet_comment SET like_count = like_count - 1 WHERE id = #{commentId} AND like_count > 0")
    int decrementLikeCount(Long commentId);
}