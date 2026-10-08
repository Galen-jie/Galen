package com.galen.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.galen.seckill.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论点赞Mapper接口
 *
 * @author Galen
 * @since 2026-10-08
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {
}