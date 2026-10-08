package com.galen.seckill.controller;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.common.Result;
import com.galen.seckill.dto.PetCommentDTO;
import com.galen.seckill.service.PetCommentService;
import com.galen.seckill.vo.PetCommentVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 宠物评论控制器
 *
 * @author Galen
 * @since 2026-10-08
 */
@Slf4j
@RestController
@RequestMapping("/pet/comment")
public class PetCommentController {

    @Autowired
    private PetCommentService petCommentService;

    /**
     * 发表评论
     */
    @PostMapping
    public Result<PetCommentVO> createComment(@Valid @RequestBody PetCommentDTO commentDTO) {
        PetCommentVO vo = petCommentService.createComment(commentDTO);
        return Result.success(vo);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        petCommentService.deleteComment(id);
        return Result.success();
    }

    /**
     * 获取评论列表
     */
    @GetMapping("/list/{petId}")
    public Result<PageResult<PetCommentVO>> getCommentList(
            @PathVariable Long petId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<PetCommentVO> result = petCommentService.getCommentList(petId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 点赞评论
     */
    @PostMapping("/{id}/like")
    public Result<Void> likeComment(@PathVariable Long id) {
        petCommentService.likeComment(id);
        return Result.success();
    }

    /**
     * 取消点赞评论
     */
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeComment(@PathVariable Long id) {
        petCommentService.unlikeComment(id);
        return Result.success();
    }
}