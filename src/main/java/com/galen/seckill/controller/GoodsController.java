package com.galen.seckill.controller;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.common.Result;
import com.galen.seckill.dto.GoodsDTO;
import com.galen.seckill.service.GoodsService;
import com.galen.seckill.vo.GoodsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/goods")
@Tag(name = "商品管理", description = "商品的新增、修改、删除、查询相关接口")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Operation(summary = "新增商品")
    @PostMapping
    public Result<GoodsVO> createGoods(@Valid @RequestBody GoodsDTO goodsDTO) {
        GoodsVO vo = goodsService.createGoods(goodsDTO);
        return Result.success(vo);
    }

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    public Result<GoodsVO> updateGoods(@PathVariable Long id, @Valid @RequestBody GoodsDTO goodsDTO) {
        GoodsVO vo = goodsService.updateGoods(id, goodsDTO);
        return Result.success(vo);
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> deleteGoods(@PathVariable Long id) {
        goodsService.deleteGoods(id);
        return Result.success();
    }

    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}")
    public Result<GoodsVO> getGoodsById(@PathVariable Long id) {
        GoodsVO vo = goodsService.getGoodsById(id);
        return Result.success(vo);
    }

    @Operation(summary = "分页查询商品列表")
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
