package com.galen.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.galen.seckill.entity.SeckillGoods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {

    @Select("SELECT * FROM seckill_goods WHERE seckill_id = #{seckillId}")
    SeckillGoods selectBySeckillId(String seckillId);

    @Update("UPDATE seckill_goods SET stock_count = stock_count - #{count}, version = version + 1 " +
            "WHERE id = #{id} AND stock_count >= #{count} AND version = #{version}")
    int decreaseStockWithVersion(@Param("id") Long id, @Param("count") Integer count, @Param("version") Integer version);
}