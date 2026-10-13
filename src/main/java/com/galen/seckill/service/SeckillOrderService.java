package com.galen.seckill.service;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.dto.SeckillOrderDTO;
import com.galen.seckill.vo.SeckillOrderVO;

public interface SeckillOrderService {

    String createSeckillOrder(SeckillOrderDTO seckillOrderDTO);

    SeckillOrderVO getOrderById(Long id);

    SeckillOrderVO getOrderByOrderNo(String orderNo);

    SeckillOrderVO getSeckillResult(String orderNo);

    PageResult<SeckillOrderVO> getMyOrders(Long userId, int pageNum, int pageSize);

    SeckillOrderVO payOrder(Long orderId, Integer payType);

    SeckillOrderVO cancelOrder(Long orderId);
}