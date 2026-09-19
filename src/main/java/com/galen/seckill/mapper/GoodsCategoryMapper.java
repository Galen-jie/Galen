package com.galen.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.galen.seckill.entity.GoodsCategory;
import org.apache.ibatis.annotations.Select;


public interface GoodsCategoryMapper extends BaseMapper<GoodsCategory> {
    @Select("SELECT * FROM goods_category WHERE category_name = #{categoryName}")
    GoodsCategory selectByName(String categoryName);
}
