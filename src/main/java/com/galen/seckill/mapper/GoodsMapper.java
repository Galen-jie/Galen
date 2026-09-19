package com.galen.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.galen.seckill.entity.Goods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface GoodsMapper extends BaseMapper<Goods> {

    @Select("SELECT * FROM goods WHERE goods_name = #{goodsName}")
    Goods selectByName(String goodsName);

    @Update("UPDATE goods SET stock = stock - #{count} WHERE id = #{id} AND stock >= #{count}")
    int decreaseStock(@Param("id") Long id, @Param("count") Integer count);

    @Update("UPDATE goods SET stock = stock + #{count} WHERE id = #{id}")
    int increaseStock(@Param("id") Long id, @Param("count") Integer count);
}