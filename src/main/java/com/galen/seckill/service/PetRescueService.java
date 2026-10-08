package com.galen.seckill.service;

import com.galen.seckill.common.PageResult;
import com.galen.seckill.dto.PetRescueDTO;
import com.galen.seckill.vo.PetRescueVO;

/**
 * 宠物救助服务接口
 *
 * @author Galen
 * @since 2026-10-08
 */
public interface PetRescueService {

    /**
     * 发布宠物救助信息
     *
     * @param petDTO 宠物信息DTO
     * @return 宠物信息VO
     */
    PetRescueVO createPet(PetRescueDTO petDTO);

    /**
     * 更新宠物信息
     *
     * @param id 宠物ID
     * @param petDTO 宠物信息DTO
     * @return 宠物信息VO
     */
    PetRescueVO updatePet(Long id, PetRescueDTO petDTO);

    /**
     * 删除宠物信息
     *
     * @param id 宠物ID
     */
    void deletePet(Long id);

    /**
     * 获取宠物详情
     *
     * @param id 宠物ID
     * @return 宠物信息VO
     */
    PetRescueVO getPetById(Long id);

    /**
     * 分页查询宠物列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param petType 宠物类型
     * @param status 状态
     * @param keyword 关键词
     * @return 分页结果
     */
    PageResult<PetRescueVO> getPetList(int pageNum, int pageSize, Integer petType, Integer status, String keyword);

    /**
     * 下架宠物信息
     *
     * @param id 宠物ID
     */
    void offlinePet(Long id);

    /**
     * 获取我发布的宠物列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<PetRescueVO> getMyPetList(int pageNum, int pageSize);

    /**
     * 点赞宠物
     *
     * @param id 宠物ID
     */
    void likePet(Long id);

    /**
     * 取消点赞
     *
     * @param id 宠物ID
     */
    void unlikePet(Long id);
}