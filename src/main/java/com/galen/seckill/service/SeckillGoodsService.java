package com.galen.seckill.service;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.dto.SeckillGoodsDTO;
import com.galen.seckill.vo.SeckillGoodsVO;

import java.util.List;

public interface SeckillGoodsService {

    SeckillGoodsVO createSeckillGoods(SeckillGoodsDTO seckillGoodsDTO);

    SeckillGoodsVO updateSeckillGoods(Long id, SeckillGoodsDTO seckillGoodsDTO);

    void deleteSeckillGoods(Long id);

    SeckillGoodsVO getSeckillGoodsById(Long id);

    SeckillGoodsVO getSeckillGoodsBySeckillId(String seckillId);

    PageResult<SeckillGoodsVO> getSeckillGoodsList(int pageNum, int pageSize, Integer status);

    List<SeckillGoodsVO> getActiveSeckillGoods();

    boolean decreaseStock(Long id, Integer count);
}