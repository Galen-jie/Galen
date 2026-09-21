package com.galen.seckill.controller;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.common.Result;
import com.galen.seckill.dto.SeckillOrderDTO;
import com.galen.seckill.service.SeckillOrderService;
import com.galen.seckill.util.UserHolder;
import com.galen.seckill.vo.SeckillOrderVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/seckill/order")
public class SeckillOrderController {

    @Autowired
    private SeckillOrderService seckillOrderService;

    @PostMapping
    public Result<SeckillOrderVO> createSeckillOrder(@Valid @RequestBody SeckillOrderDTO seckillOrderDTO) {
        SeckillOrderVO vo = seckillOrderService.createSeckillOrder(seckillOrderDTO);
        return Result.success(vo);
    }

    @GetMapping("/{id}")
    public Result<SeckillOrderVO> getOrderById(@PathVariable Long id) {
        SeckillOrderVO vo = seckillOrderService.getOrderById(id);
        return Result.success(vo);
    }

    @GetMapping("/orderNo/{orderNo}")
    public Result<SeckillOrderVO> getOrderByOrderNo(@PathVariable String orderNo) {
        SeckillOrderVO vo = seckillOrderService.getOrderByOrderNo(orderNo);
        return Result.success(vo);
    }

    @GetMapping("/my")
    public Result<PageResult<SeckillOrderVO>> getMyOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserHolder.getUser().getId();
        PageResult<SeckillOrderVO> result = seckillOrderService.getMyOrders(userId, pageNum, pageSize);
        return Result.success(result);
    }

    @PostMapping("/{orderId}/pay")
    public Result<SeckillOrderVO> payOrder(
            @PathVariable Long orderId,
            @RequestParam Integer payType) {
        SeckillOrderVO vo = seckillOrderService.payOrder(orderId, payType);
        return Result.success(vo);
    }

    @PostMapping("/{orderId}/cancel")
    public Result<SeckillOrderVO> cancelOrder(@PathVariable Long orderId) {
        SeckillOrderVO vo = seckillOrderService.cancelOrder(orderId);
        return Result.success(vo);
    }
}