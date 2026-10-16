package com.galen.seckill.controller;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.common.Result;
import com.galen.seckill.dto.SeckillGoodsDTO;
import com.galen.seckill.service.SeckillGoodsService;
import com.galen.seckill.vo.SeckillGoodsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/seckill/goods")
@Tag(name = "秒杀商品管理", description = "秒杀商品的新增、修改、删除、查询相关接口")
public class SeckillGoodsController {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @Operation(summary = "新增秒杀商品")
    @PostMapping
    public Result<SeckillGoodsVO> createSeckillGoods(@Valid @RequestBody SeckillGoodsDTO seckillGoodsDTO) {
        SeckillGoodsVO vo = seckillGoodsService.createSeckillGoods(seckillGoodsDTO);
        return Result.success(vo);
    }

    @Operation(summary = "更新秒杀商品")
    @PutMapping("/{id}")
    public Result<SeckillGoodsVO> updateSeckillGoods(@PathVariable Long id, @Valid @RequestBody SeckillGoodsDTO seckillGoodsDTO) {
        SeckillGoodsVO vo = seckillGoodsService.updateSeckillGoods(id, seckillGoodsDTO);
        return Result.success(vo);
    }

    @Operation(summary = "删除秒杀商品")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSeckillGoods(@PathVariable Long id) {
        seckillGoodsService.deleteSeckillGoods(id);
        return Result.success();
    }

    @Operation(summary = "获取秒杀商品详情")
    @GetMapping("/{id}")
    public Result<SeckillGoodsVO> getSeckillGoodsById(@PathVariable Long id) {
        SeckillGoodsVO vo = seckillGoodsService.getSeckillGoodsById(id);
        return Result.success(vo);
    }

    @Operation(summary = "根据秒杀活动ID查询秒杀商品")
    @GetMapping("/seckillId/{seckillId}")
    public Result<SeckillGoodsVO> getSeckillGoodsBySeckillId(@PathVariable String seckillId) {
        SeckillGoodsVO vo = seckillGoodsService.getSeckillGoodsBySeckillId(seckillId);
        return Result.success(vo);
    }

    @Operation(summary = "分页查询秒杀商品列表")
    @GetMapping("/list")
    public Result<PageResult<SeckillGoodsVO>> getSeckillGoodsList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        PageResult<SeckillGoodsVO> result = seckillGoodsService.getSeckillGoodsList(pageNum, pageSize, status);
        return Result.success(result);
    }

    @Operation(summary = "获取当前进行中的秒杀商品")
    @GetMapping("/active")
    public Result<List<SeckillGoodsVO>> getActiveSeckillGoods() {
        List<SeckillGoodsVO> list = seckillGoodsService.getActiveSeckillGoods();
        return Result.success(list);
    }
}
