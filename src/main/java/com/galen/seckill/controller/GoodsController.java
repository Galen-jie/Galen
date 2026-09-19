package com.galen.seckill.controller;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.common.Result;
import com.galen.seckill.dto.GoodsDTO;
import com.galen.seckill.service.GoodsService;
import com.galen.seckill.vo.GoodsVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @PostMapping
    public Result<GoodsVO> createGoods(@Valid @RequestBody GoodsDTO goodsDTO) {
        GoodsVO vo = goodsService.createGoods(goodsDTO);
        return Result.success(vo);
    }

    @PutMapping("/{id}")
    public Result<GoodsVO> updateGoods(@PathVariable Long id, @Valid @RequestBody GoodsDTO goodsDTO) {
        GoodsVO vo = goodsService.updateGoods(id, goodsDTO);
        return Result.success(vo);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteGoods(@PathVariable Long id) {
        goodsService.deleteGoods(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<GoodsVO> getGoodsById(@PathVariable Long id) {
        GoodsVO vo = goodsService.getGoodsById(id);
        return Result.success(vo);
    }

    @GetMapping("/list")
    public Result<PageResult<GoodsVO>> getGoodsList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        PageResult<GoodsVO> result = goodsService.getGoodsList(pageNum, pageSize, categoryId, status);
        return Result.success(result);
    }
}