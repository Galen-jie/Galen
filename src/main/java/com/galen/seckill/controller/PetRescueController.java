package com.galen.seckill.controller;

import com.galen.seckill.annotation.RequireRole;
import com.galen.seckill.common.PageResult;
import com.galen.seckill.common.Result;
import com.galen.seckill.constant.UserRole;
import com.galen.seckill.dto.PetRescueDTO;
import com.galen.seckill.service.PetRescueService;
import com.galen.seckill.vo.PetRescueVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 宠物救助信息控制器
 *
 * @author Galen
 * @since 2026-10-08
 */
@Slf4j
@RestController
@RequestMapping("/pet")
public class PetRescueController {

    @Autowired
    private PetRescueService petRescueService;

    /**
     * 发布宠物救助信息（需要救助站/志愿者权限）
     */
    @PostMapping
    @RequireRole(UserRole.RESCUE_STATION)
    public Result<PetRescueVO> createPet(@Valid @RequestBody PetRescueDTO petDTO) {
        PetRescueVO vo = petRescueService.createPet(petDTO);
        return Result.success(vo);
    }

    /**
     * 更新宠物信息（需要发布者权限）
     */
    @PutMapping("/{id}")
    public Result<PetRescueVO> updatePet(@PathVariable Long id,
                                          @Valid @RequestBody PetRescueDTO petDTO) {
        PetRescueVO vo = petRescueService.updatePet(id, petDTO);
        return Result.success(vo);
    }

    /**
     * 删除宠物信息（需要发布者权限）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePet(@PathVariable Long id) {
        petRescueService.deletePet(id);
        return Result.success();
    }

    /**
     * 获取宠物详情
     */
    @GetMapping("/{id}")
    public Result<PetRescueVO> getPetById(@PathVariable Long id) {
        PetRescueVO vo = petRescueService.getPetById(id);
        return Result.success(vo);
    }

    /**
     * 分页查询宠物列表
     */
    @GetMapping("/list")
    public Result<PageResult<PetRescueVO>> getPetList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer petType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        PageResult<PetRescueVO> result = petRescueService.getPetList(pageNum, pageSize, petType, status, keyword);
        return Result.success(result);
    }

    /**
     * 下架宠物信息（需要发布者权限）
     */
    @PutMapping("/{id}/offline")
    public Result<Void> offlinePet(@PathVariable Long id) {
        petRescueService.offlinePet(id);
        return Result.success();
    }

    /**
     * 获取我发布的宠物列表
     */
    @GetMapping("/my")
    public Result<PageResult<PetRescueVO>> getMyPetList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<PetRescueVO> result = petRescueService.getMyPetList(pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 点赞宠物
     */
    @PostMapping("/{id}/like")
    public Result<Void> likePet(@PathVariable Long id) {
        petRescueService.likePet(id);
        return Result.success();
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikePet(@PathVariable Long id) {
        petRescueService.unlikePet(id);
        return Result.success();
    }
}