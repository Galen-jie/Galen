package com.galen.seckill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.galen.seckill.common.PageResult;
import com.galen.seckill.common.ResultCode;
import com.galen.seckill.constant.ApplicationStatus;
import com.galen.seckill.constant.PetStatus;
import com.galen.seckill.dto.AdoptionApplicationDTO;
import com.galen.seckill.dto.ApplicationReviewDTO;
import com.galen.seckill.entity.AdoptionApplication;
import com.galen.seckill.entity.PetRescue;
import com.galen.seckill.entity.User;
import com.galen.seckill.exception.BusinessException;
import com.galen.seckill.mapper.AdoptionApplicationMapper;
import com.galen.seckill.mapper.PetRescueMapper;
import com.galen.seckill.mapper.UserMapper;
import com.galen.seckill.service.AdoptionApplicationService;
import com.galen.seckill.util.UserHolder;
import com.galen.seckill.vo.AdoptionApplicationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 领养申请服务实现类
 *
 * @author Galen
 * @since 2026-10-08
 */
@Slf4j
@Service
public class AdoptionApplicationServiceImpl implements AdoptionApplicationService {

    @Autowired
    private AdoptionApplicationMapper adoptionApplicationMapper;

    @Autowired
    private PetRescueMapper petRescueMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public AdoptionApplicationVO createApplication(AdoptionApplicationDTO applicationDTO) {
        Long userId = UserHolder.getUser().getId();
        Long petId = applicationDTO.getPetId();

        // 检查宠物是否存在且可领养
        PetRescue pet = petRescueMapper.selectById(petId);
        if (pet == null) {
            throw new BusinessException(ResultCode.PET_NOT_FOUND);
        }
        if (pet.getStatus() == null || pet.getStatus() != PetStatus.WAITING) {
            throw new BusinessException(ResultCode.PET_ALREADY_ADOPTED);
        }

        // 检查是否已提交过申请
        LambdaQueryWrapper<AdoptionApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdoptionApplication::getPetId, petId)
                .eq(AdoptionApplication::getApplicantId, userId)
                .ne(AdoptionApplication::getStatus, ApplicationStatus.CANCELLED);
        if (adoptionApplicationMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.APPLICATION_ALREADY_EXISTS);
        }

        // 创建申请
        AdoptionApplication application = BeanUtil.copyProperties(applicationDTO, AdoptionApplication.class);
        application.setApplicationNo(generateApplicationNo());
        application.setApplicantId(userId);
        application.setStatus(ApplicationStatus.PENDING);
        adoptionApplicationMapper.insert(application);

        log.info("用户{}提交领养申请: {}", userId, application.getApplicationNo());

        return convertToVO(application);
    }

    @Override
    @Transactional
    public void cancelApplication(Long id) {
        Long userId = UserHolder.getUser().getId();

        // 查询申请
        AdoptionApplication application = adoptionApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(ResultCode.APPLICATION_NOT_FOUND);
        }

        // 检查权限
        if (!application.getApplicantId().equals(userId)) {
            throw new BusinessException(ResultCode.APPLICATION_NO_PERMISSION);
        }

        // 检查申请状态
        if (application.getStatus() == null || application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException(ResultCode.APPLICATION_ALREADY_PROCESSED);
        }

        // 更新状态为已取消
        application.setStatus(ApplicationStatus.CANCELLED);
        adoptionApplicationMapper.updateById(application);

        log.info("用户{}取消领养申请: {}", userId, id);
    }

    @Override
    @Transactional
    public void reviewApplication(ApplicationReviewDTO reviewDTO) {
        User reviewer = UserHolder.getUser();

        // 查询申请
        AdoptionApplication application = adoptionApplicationMapper.selectById(reviewDTO.getApplicationId());
        if (application == null) {
            throw new BusinessException(ResultCode.APPLICATION_NOT_FOUND);
        }

        // 检查申请状态
        if (application.getStatus() == null || application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException(ResultCode.APPLICATION_ALREADY_PROCESSED);
        }

        // 检查是否有审核权限（必须是该宠物的发布者）
        PetRescue pet = petRescueMapper.selectById(application.getPetId());
        if (!pet.getPublisherId().equals(reviewer.getId())) {
            throw new BusinessException(ResultCode.APPLICATION_NO_PERMISSION);
        }

        // 更新申请状态
        application.setStatus(reviewDTO.getStatus());
        application.setReviewerId(reviewer.getId());
        application.setReviewTime(LocalDateTime.now());
        if (reviewDTO.getStatus() != null && reviewDTO.getStatus() == ApplicationStatus.REJECTED) {
            application.setRejectReason(reviewDTO.getRejectReason());
        }
        adoptionApplicationMapper.updateById(application);

        // 如果通过，更新宠物状态为"已预约"
        if (reviewDTO.getStatus() != null && reviewDTO.getStatus() == ApplicationStatus.APPROVED) {
            pet.setStatus(PetStatus.RESERVED);
            petRescueMapper.updateById(pet);
        }

        log.info("审核领养申请: {}, 结果: {}", application.getApplicationNo(), reviewDTO.getStatus());
    }

    @Override
    public AdoptionApplicationVO getApplicationById(Long id) {
        AdoptionApplication application = adoptionApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(ResultCode.APPLICATION_NOT_FOUND);
        }
        return convertToVO(application);
    }

    @Override
    public PageResult<AdoptionApplicationVO> getMyApplications(int pageNum, int pageSize) {
        Long userId = UserHolder.getUser().getId();

        // 构建查询条件
        LambdaQueryWrapper<AdoptionApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdoptionApplication::getApplicantId, userId)
                .orderByDesc(AdoptionApplication::getCreateTime);

        // 分页查询
        Page<AdoptionApplication> page = new Page<>(pageNum, pageSize);
        Page<AdoptionApplication> result = adoptionApplicationMapper.selectPage(page, wrapper);

        // 转换为VO
        List<AdoptionApplicationVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<AdoptionApplicationVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setPages(result.getPages());
        pageResult.setRecords(voList);

        return pageResult;
    }

    @Override
    public PageResult<AdoptionApplicationVO> getPendingApplications(int pageNum, int pageSize) {
        Long userId = UserHolder.getUser().getId();

        // 查询我发布的宠物ID列表
        LambdaQueryWrapper<PetRescue> petWrapper = new LambdaQueryWrapper<>();
        petWrapper.eq(PetRescue::getPublisherId, userId)
                .select(PetRescue::getId);
        List<Long> petIds = petRescueMapper.selectList(petWrapper).stream()
                .map(PetRescue::getId)
                .collect(Collectors.toList());

        if (petIds.isEmpty()) {
            return new PageResult<>();
        }

        // 查询这些宠物的待审核申请
        LambdaQueryWrapper<AdoptionApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AdoptionApplication::getPetId, petIds)
                .eq(AdoptionApplication::getStatus, ApplicationStatus.PENDING)
                .orderByDesc(AdoptionApplication::getCreateTime);

        // 分页查询
        Page<AdoptionApplication> page = new Page<>(pageNum, pageSize);
        Page<AdoptionApplication> result = adoptionApplicationMapper.selectPage(page, wrapper);

        // 转换为VO
        List<AdoptionApplicationVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<AdoptionApplicationVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setPages(result.getPages());
        pageResult.setRecords(voList);

        return pageResult;
    }

    @Override
    public PageResult<AdoptionApplicationVO> getApplicationsByPet(Long petId, int pageNum, int pageSize) {
        // 构建查询条件
        LambdaQueryWrapper<AdoptionApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdoptionApplication::getPetId, petId)
                .orderByDesc(AdoptionApplication::getCreateTime);

        // 分页查询
        Page<AdoptionApplication> page = new Page<>(pageNum, pageSize);
        Page<AdoptionApplication> result = adoptionApplicationMapper.selectPage(page, wrapper);

        // 转换为VO
        List<AdoptionApplicationVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<AdoptionApplicationVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setPages(result.getPages());
        pageResult.setRecords(voList);

        return pageResult;
    }

    /**
     * 生成申请编号
     */
    private String generateApplicationNo() {
        return "APP" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 转换为VO
     */
    private AdoptionApplicationVO convertToVO(AdoptionApplication application) {
        AdoptionApplicationVO vo = BeanUtil.copyProperties(application, AdoptionApplicationVO.class);

        // 设置状态名称
        vo.setStatusName(ApplicationStatus.getName(application.getStatus()));

        // 查询宠物信息
        PetRescue pet = petRescueMapper.selectById(application.getPetId());
        if (pet != null) {
            vo.setPetName(pet.getPetName());
        }

        // 查询审核人信息
        if (application.getReviewerId() != null) {
            User reviewer = userMapper.selectById(application.getReviewerId());
            if (reviewer != null) {
                vo.setReviewerName(reviewer.getNickname());
            }
        }

        return vo;
    }
}