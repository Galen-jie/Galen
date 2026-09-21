package com.galen.seckill.controller;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.common.Result;
import com.galen.seckill.dto.SeckillGoodsDTO;
import com.galen.seckill.service.SeckillGoodsService;
import com.galen.seckill.vo.SeckillGoodsVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/seckill/goods")
public class SeckillGoodsController {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @PostMapping
    public Result<SeckillGoodsVO> createSeckillGoods(@Valid @RequestBody SeckillGoodsDTO seckillGoodsDTO) {
        SeckillGoodsVO vo = seckillGoodsService.createSeckillGoods(seckillGoodsDTO);
        return Result.success(vo);
    }

    @PutMapping("/{id}")
    public Result<SeckillGoodsVO> updateSeckillGoods(@PathVariable Long id, @Valid @RequestBody SeckillGoodsDTO seckillGoodsDTO) {
        SeckillGoodsVO vo = seckillGoodsService.updateSeckillGoods(id, seckillGoodsDTO);
        return Result.success(vo);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteSeckillGoods(@PathVariable Long id) {
        seckillGoodsService.deleteSeckillGoods(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<SeckillGoodsVO> getSeckillGoodsById(@PathVariable Long id) {
        SeckillGoodsVO vo = seckillGoodsService.getSeckillGoodsById(id);
        return Result.success(vo);
    }

    @GetMapping("/seckillId/{seckillId}")
    public Result<SeckillGoodsVO> getSeckillGoodsBySeckillId(@PathVariable String seckillId) {
        SeckillGoodsVO vo = seckillGoodsService.getSeckillGoodsBySeckillId(seckillId);
        return Result.success(vo);
    }

    @GetMapping("/list")
    public Result<PageResult<SeckillGoodsVO>> getSeckillGoodsList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        PageResult<SeckillGoodsVO> result = seckillGoodsService.getSeckillGoodsList(pageNum, pageSize, status);
        return Result.success(result);
    }

    @GetMapping("/active")
    public Result<List<SeckillGoodsVO>> getActiveSeckillGoods() {
        List<SeckillGoodsVO> list = seckillGoodsService.getActiveSeckillGoods();
        return Result.success(list);
    }
}