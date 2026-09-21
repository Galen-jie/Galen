package com.galen.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.galen.seckill.entity.SeckillOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {

    @Select("SELECT * FROM seckill_order WHERE order_no = #{orderNo}")
    SeckillOrder selectByOrderNo(String orderNo);

    @Select("SELECT * FROM seckill_order WHERE user_id = #{userId} AND seckill_id = #{seckillId} AND goods_id = #{goodsId}")
    SeckillOrder selectByUserIdAndSeckillId(@Param("userId") Long userId, @Param("seckillId") String seckillId, @Param("goodsId") Long goodsId);
}