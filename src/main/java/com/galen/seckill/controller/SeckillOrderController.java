package com.galen.seckill.controller;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.common.Result;
import com.galen.seckill.dto.SeckillOrderDTO;
import com.galen.seckill.service.SeckillOrderService;
import com.galen.seckill.util.UserHolder;
import com.galen.seckill.vo.SeckillOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/seckill/order")
@Tag(name = "秒杀订单管理", description = "秒杀订单的创建、查询、支付、取消相关接口")
public class SeckillOrderController {

    @Autowired
    private SeckillOrderService seckillOrderService;

    @Operation(summary = "创建秒杀订单")
    @PostMapping
    public Result<String> createSeckillOrder(@Valid @RequestBody SeckillOrderDTO seckillOrderDTO) {
        String orderNo = seckillOrderService.createSeckillOrder(seckillOrderDTO);
        return Result.success("秒杀请求已提交，请轮询查询结果", orderNo);
    }

    @Operation(summary = "查询秒杀结果")
    @GetMapping("/result/{orderNo}")
    public Result<SeckillOrderVO> getSeckillResult(@PathVariable String orderNo) {
        SeckillOrderVO vo = seckillOrderService.getSeckillResult(orderNo);
        if (vo == null) {
            return Result.success("订单处理中，请稍后重试", null);
        }
        return Result.success(vo);
    }

    @Operation(summary = "根据ID获取订单")
    @GetMapping("/{id}")
    public Result<SeckillOrderVO> getOrderById(@PathVariable Long id) {
        SeckillOrderVO vo = seckillOrderService.getOrderById(id);
        return Result.success(vo);
    }

    @Operation(summary = "根据订单号获取订单")
    @GetMapping("/orderNo/{orderNo}")
    public Result<SeckillOrderVO> getOrderByOrderNo(@PathVariable String orderNo) {
        SeckillOrderVO vo = seckillOrderService.getOrderByOrderNo(orderNo);
        return Result.success(vo);
    }

    @Operation(summary = "获取我的订单列表")
    @GetMapping("/my")
    public Result<PageResult<SeckillOrderVO>> getMyOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserHolder.getUser().getId();
        PageResult<SeckillOrderVO> result = seckillOrderService.getMyOrders(userId, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "支付订单")
    @PostMapping("/{orderId}/pay")
    public Result<SeckillOrderVO> payOrder(
            @PathVariable Long orderId,
            @RequestParam Integer payType) {
        SeckillOrderVO vo = seckillOrderService.payOrder(orderId, payType);
        return Result.success(vo);
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{orderId}/cancel")
    public Result<SeckillOrderVO> cancelOrder(@PathVariable Long orderId) {
        SeckillOrderVO vo = seckillOrderService.cancelOrder(orderId);
        return Result.success(vo);
    }
}
