package com.galen.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.galen.seckill.entity.PetLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物点赞Mapper接口
 *
 * @author Galen
 * @since 2026-10-08
 */
@Mapper
public interface PetLikeMapper extends BaseMapper<PetLike> {
}