package com.galen.seckill.controller;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.common.Result;
import com.galen.seckill.dto.GoodsCategoryDTO;
import com.galen.seckill.service.GoodsCategoryService;
import com.galen.seckill.vo.GoodsCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
@Tag(name = "商品分类管理", description = "商品分类的新增、修改、删除、查询相关接口")
public class GoodsCategoryController {

    @Autowired
    private GoodsCategoryService goodsCategoryService;

    @Operation(summary = "新增分类")
    @PostMapping
    public Result<GoodsCategoryVO> createCategory(@Valid @RequestBody GoodsCategoryDTO goodsCategoryDTO) {
        GoodsCategoryVO vo = goodsCategoryService.createCategory(goodsCategoryDTO);
        return Result.success(vo);
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<GoodsCategoryVO> updateCategory(@PathVariable Long id,
                                                   @Valid @RequestBody GoodsCategoryDTO goodsCategoryDTO) {
        GoodsCategoryVO vo = goodsCategoryService.updateCategory(id, goodsCategoryDTO);
        return Result.success(vo);
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        goodsCategoryService.deleteCategory(id);
        return Result.success();
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<GoodsCategoryVO> getCategoryById(@PathVariable Long id) {
        GoodsCategoryVO vo = goodsCategoryService.getCategoryById(id);
        return Result.success(vo);
    }

    @Operation(summary = "分页查询分类列表")
    @GetMapping("/list")
    public Result<PageResult<GoodsCategoryVO>> getCategoryList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<GoodsCategoryVO> result = goodsCategoryService.getCategoryList(pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public Result<List<GoodsCategoryVO>> getCategoryTree() {
        List<GoodsCategoryVO> tree = goodsCategoryService.getCategoryTree();
        return Result.success(tree);
    }
}
