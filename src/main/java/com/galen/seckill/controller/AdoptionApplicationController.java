package com.galen.seckill.controller;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.common.Result;
import com.galen.seckill.dto.AdoptionApplicationDTO;
import com.galen.seckill.dto.ApplicationReviewDTO;
import com.galen.seckill.service.AdoptionApplicationService;
import com.galen.seckill.vo.AdoptionApplicationVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 领养申请控制器
 *
 * @author Galen
 * @since 2026-10-08
 */
@Slf4j
@RestController
@RequestMapping("/adoption")
public class AdoptionApplicationController {

    @Autowired
    private AdoptionApplicationService adoptionApplicationService;

    /**
     * 提交领养申请（普通用户）
     */
    @PostMapping
    public Result<AdoptionApplicationVO> createApplication(@Valid @RequestBody AdoptionApplicationDTO applicationDTO) {
        AdoptionApplicationVO vo = adoptionApplicationService.createApplication(applicationDTO);
        return Result.success(vo);
    }

    /**
     * 取消领养申请（申请人）
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelApplication(@PathVariable Long id) {
        adoptionApplicationService.cancelApplication(id);
        return Result.success();
    }

    /**
     * 审核领养申请（救助站/志愿者）
     */
    @PutMapping("/review")
    public Result<Void> reviewApplication(@Valid @RequestBody ApplicationReviewDTO reviewDTO) {
        adoptionApplicationService.reviewApplication(reviewDTO);
        return Result.success();
    }

    /**
     * 获取申请详情
     */
    @GetMapping("/{id}")
    public Result<AdoptionApplicationVO> getApplicationById(@PathVariable Long id) {
        AdoptionApplicationVO vo = adoptionApplicationService.getApplicationById(id);
        return Result.success(vo);
    }

    /**
     * 获取我提交的申请列表（普通用户）
     */
    @GetMapping("/my")
    public Result<PageResult<AdoptionApplicationVO>> getMyApplications(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<AdoptionApplicationVO> result = adoptionApplicationService.getMyApplications(pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取待审核的申请列表（救助站/志愿者）
     */
    @GetMapping("/pending")
    public Result<PageResult<AdoptionApplicationVO>> getPendingApplications(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<AdoptionApplicationVO> result = adoptionApplicationService.getPendingApplications(pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取某宠物的申请列表（发布者）
     */
    @GetMapping("/pet/{petId}")
    public Result<PageResult<AdoptionApplicationVO>> getApplicationsByPet(
            @PathVariable Long petId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<AdoptionApplicationVO> result = adoptionApplicationService.getApplicationsByPet(petId, pageNum, pageSize);
        return Result.success(result);
    }
}