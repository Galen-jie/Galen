package com.galen.seckill.service;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.dto.GoodsDTO;
import com.galen.seckill.vo.GoodsVO;

public interface GoodsService {

    GoodsVO createGoods(GoodsDTO goodsDTO);

    GoodsVO updateGoods(Long id, GoodsDTO goodsDTO);

    void deleteGoods(Long id);

    GoodsVO getGoodsById(Long id);

    PageResult<GoodsVO> getGoodsList(int pageNum, int pageSize, Long categoryId, Integer status);

    boolean decreaseStock(Long id, Integer count);

    boolean increaseStock(Long id, Integer count);
}