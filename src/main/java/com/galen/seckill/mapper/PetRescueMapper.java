package com.galen.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.galen.seckill.entity.PetRescue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 宠物救助Mapper接口
 *
 * @author Galen
 * @since 2026-10-08
 */
@Mapper
public interface PetRescueMapper extends BaseMapper<PetRescue> {

    /**
     * 点赞数+1
     */
    @Update("UPDATE pet_rescue SET like_count = like_count + 1 WHERE id = #{petId}")
    int incrementLikeCount(Long petId);

    /**
     * 点赞数-1
     */
    @Update("UPDATE pet_rescue SET like_count = like_count - 1 WHERE id = #{petId} AND like_count > 0")
    int decrementLikeCount(Long petId);

    /**
     * 评论数+1
     */
    @Update("UPDATE pet_rescue SET comment_count = comment_count + 1 WHERE id = #{petId}")
    int incrementCommentCount(Long petId);

    /**
     * 评论数-1
     */
    @Update("UPDATE pet_rescue SET comment_count = comment_count - 1 WHERE id = #{petId} AND comment_count > 0")
    int decrementCommentCount(Long petId);

    /**
     * 更新浏览量
     */
    @Update("UPDATE pet_rescue SET view_count = #{viewCount} WHERE id = #{petId}")
    int updateViewCount(Long petId, Integer viewCount);
}