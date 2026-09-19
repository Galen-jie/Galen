package com.galen.seckill.controller;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.common.Result;
import com.galen.seckill.dto.GoodsCategoryDTO;
import com.galen.seckill.service.GoodsCategoryService;
import com.galen.seckill.vo.GoodsCategoryVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class GoodsCategoryController {

    @Autowired
    private GoodsCategoryService goodsCategoryService;

    @PostMapping
    public Result<GoodsCategoryVO> createCategory(@Valid @RequestBody GoodsCategoryDTO goodsCategoryDTO) {
        GoodsCategoryVO vo = goodsCategoryService.createCategory(goodsCategoryDTO);
        return Result.success(vo);
    }

    @PutMapping("/{id}")
    public Result<GoodsCategoryVO> updateCategory(@PathVariable Long id,
                                                   @Valid @RequestBody GoodsCategoryDTO goodsCategoryDTO) {
        GoodsCategoryVO vo = goodsCategoryService.updateCategory(id, goodsCategoryDTO);
        return Result.success(vo);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        goodsCategoryService.deleteCategory(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<GoodsCategoryVO> getCategoryById(@PathVariable Long id) {
        GoodsCategoryVO vo = goodsCategoryService.getCategoryById(id);
        return Result.success(vo);
    }

    @GetMapping("/list")
    public Result<PageResult<GoodsCategoryVO>> getCategoryList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<GoodsCategoryVO> result = goodsCategoryService.getCategoryList(pageNum, pageSize);
        return Result.success(result);
    }

    @GetMapping("/tree")
    public Result<List<GoodsCategoryVO>> getCategoryTree() {
        List<GoodsCategoryVO> tree = goodsCategoryService.getCategoryTree();
        return Result.success(tree);
    }
}