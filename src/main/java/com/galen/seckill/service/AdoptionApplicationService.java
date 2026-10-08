package com.galen.seckill.service;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.dto.AdoptionApplicationDTO;
import com.galen.seckill.dto.ApplicationReviewDTO;
import com.galen.seckill.vo.AdoptionApplicationVO;

/**
 * 领养申请服务接口
 *
 * @author Galen
 * @since 2026-10-08
 */
public interface AdoptionApplicationService {

    /**
     * 提交领养申请
     *
     * @param applicationDTO 申请DTO
     * @return 申请VO
     */
    AdoptionApplicationVO createApplication(AdoptionApplicationDTO applicationDTO);

    /**
     * 取消领养申请
     *
     * @param id 申请ID
     */
    void cancelApplication(Long id);

    /**
     * 审核领养申请
     *
     * @param reviewDTO 审核DTO
     */
    void reviewApplication(ApplicationReviewDTO reviewDTO);

    /**
     * 获取申请详情
     *
     * @param id 申请ID
     * @return 申请VO
     */
    AdoptionApplicationVO getApplicationById(Long id);

    /**
     * 获取我提交的申请列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<AdoptionApplicationVO> getMyApplications(int pageNum, int pageSize);

    /**
     * 获取待审核的申请列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<AdoptionApplicationVO> getPendingApplications(int pageNum, int pageSize);

    /**
     * 获取某宠物的申请列表
     *
     * @param petId 宠物ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<AdoptionApplicationVO> getApplicationsByPet(Long petId, int pageNum, int pageSize);
}