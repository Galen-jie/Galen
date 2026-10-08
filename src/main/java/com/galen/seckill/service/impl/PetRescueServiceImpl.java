package com.galen.seckill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.galen.seckill.common.PageResult;
import com.galen.seckill.common.ResultCode;
import com.galen.seckill.constant.PetStatus;
import com.galen.seckill.constant.PetType;
import com.galen.seckill.dto.PetRescueDTO;
import com.galen.seckill.entity.PetLike;
import com.galen.seckill.entity.PetRescue;
import com.galen.seckill.entity.User;
import com.galen.seckill.exception.BusinessException;
import com.galen.seckill.mapper.PetLikeMapper;
import com.galen.seckill.mapper.PetRescueMapper;
import com.galen.seckill.mapper.UserMapper;
import com.galen.seckill.service.PetRescueService;
import com.galen.seckill.util.UserHolder;
import com.galen.seckill.vo.PetRescueVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 宠物救助服务实现类
 *
 * @author Galen
 * @since 2026-10-08
 */
@Slf4j
@Service
public class PetRescueServiceImpl implements PetRescueService {

    @Autowired
    private PetRescueMapper petRescueMapper;

    @Autowired
    private PetLikeMapper petLikeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String VIEW_KEY_PREFIX = "galen:pet:view:";
    private static final String LIKE_KEY_PREFIX = "galen:pet:like:";

    @Override
    public PetRescueVO createPet(PetRescueDTO petDTO) {
        Long userId = UserHolder.getUser().getId();

        // 转换DTO为Entity
        PetRescue pet = BeanUtil.copyProperties(petDTO, PetRescue.class);
        pet.setPublisherId(userId);
        pet.setStatus(PetStatus.WAITING);
        pet.setViewCount(0);
        pet.setLikeCount(0);
        pet.setCommentCount(0);

        // 处理图片URL列表，转为JSON字符串
        if (petDTO.getImageUrls() != null && !petDTO.getImageUrls().isEmpty()) {
            pet.setImages(JSONUtil.toJsonStr(petDTO.getImageUrls()));
        }

        // 保存到数据库
        petRescueMapper.insert(pet);

        log.info("用户{}发布宠物信息: {}", userId, pet.getId());

        return convertToVO(pet);
    }

    @Override
    public PetRescueVO updatePet(Long id, PetRescueDTO petDTO) {
        Long userId = UserHolder.getUser().getId();

        // 查询宠物信息
        PetRescue pet = petRescueMapper.selectById(id);
        if (pet == null) {
            throw new BusinessException(ResultCode.PET_NOT_FOUND);
        }

        // 检查权限
        if (!pet.getPublisherId().equals(userId)) {
            throw new BusinessException(ResultCode.PET_NO_PERMISSION);
        }

        // 更新宠物信息
        BeanUtil.copyProperties(petDTO, pet, "id", "publisherId", "status");
        if (petDTO.getImageUrls() != null && !petDTO.getImageUrls().isEmpty()) {
            pet.setImages(JSONUtil.toJsonStr(petDTO.getImageUrls()));
        }

        petRescueMapper.updateById(pet);

        log.info("用户{}更新宠物信息: {}", userId, id);

        return convertToVO(pet);
    }

    @Override
    public void deletePet(Long id) {
        Long userId = UserHolder.getUser().getId();

        // 查询宠物信息
        PetRescue pet = petRescueMapper.selectById(id);
        if (pet == null) {
            throw new BusinessException(ResultCode.PET_NOT_FOUND);
        }

        // 检查权限
        if (!pet.getPublisherId().equals(userId)) {
            throw new BusinessException(ResultCode.PET_NO_PERMISSION);
        }

        // 删除宠物信息
        petRescueMapper.deleteById(id);

        log.info("用户{}删除宠物信息: {}", userId, id);
    }

    @Override
    public PetRescueVO getPetById(Long id) {
        Long userId = UserHolder.getUser().getId();

        // 查询宠物信息
        PetRescue pet = petRescueMapper.selectById(id);
        if (pet == null) {
            throw new BusinessException(ResultCode.PET_NOT_FOUND);
        }

        // 浏览量+1（使用Redis）
        String viewKey = VIEW_KEY_PREFIX + id;
        redisTemplate.opsForValue().increment(viewKey);

        // 检查是否点赞
        String likeKey = LIKE_KEY_PREFIX + id;
        Boolean isLiked = redisTemplate.opsForSet().isMember(likeKey, userId.toString());

        // 转换为VO
        PetRescueVO vo = convertToVO(pet);
        vo.setIsLiked(Boolean.TRUE.equals(isLiked));

        return vo;
    }

    @Override
    public PageResult<PetRescueVO> getPetList(int pageNum, int pageSize, Integer petType, Integer status, String keyword) {
        // 构建查询条件
        LambdaQueryWrapper<PetRescue> wrapper = new LambdaQueryWrapper<>();

        if (petType != null) {
            wrapper.eq(PetRescue::getPetType, petType);
        }
        if (status != null) {
            wrapper.eq(PetRescue::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(PetRescue::getPetName, keyword)
                    .or()
                    .like(PetRescue::getDescription, keyword);
        }

        // 按创建时间倒序
        wrapper.orderByDesc(PetRescue::getCreateTime);

        // 分页查询
        Page<PetRescue> page = new Page<>(pageNum, pageSize);
        Page<PetRescue> result = petRescueMapper.selectPage(page, wrapper);

        // 转换为VO
        List<PetRescueVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<PetRescueVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setPages(result.getPages());
        pageResult.setRecords(voList);

        return pageResult;
    }

    @Override
    public void offlinePet(Long id) {
        Long userId = UserHolder.getUser().getId();

        // 查询宠物信息
        PetRescue pet = petRescueMapper.selectById(id);
        if (pet == null) {
            throw new BusinessException(ResultCode.PET_NOT_FOUND);
        }

        // 检查权限
        if (!pet.getPublisherId().equals(userId)) {
            throw new BusinessException(ResultCode.PET_NO_PERMISSION);
        }

        // 更新状态为已下架
        pet.setStatus(PetStatus.OFFLINE);
        petRescueMapper.updateById(pet);

        log.info("用户{}下架宠物信息: {}", userId, id);
    }

    @Override
    public PageResult<PetRescueVO> getMyPetList(int pageNum, int pageSize) {
        Long userId = UserHolder.getUser().getId();

        // 构建查询条件
        LambdaQueryWrapper<PetRescue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetRescue::getPublisherId, userId)
                .orderByDesc(PetRescue::getCreateTime);

        // 分页查询
        Page<PetRescue> page = new Page<>(pageNum, pageSize);
        Page<PetRescue> result = petRescueMapper.selectPage(page, wrapper);

        // 转换为VO
        List<PetRescueVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<PetRescueVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setPages(result.getPages());
        pageResult.setRecords(voList);

        return pageResult;
    }

    @Override
    public void likePet(Long id) {
        Long userId = UserHolder.getUser().getId();
        String likeKey = LIKE_KEY_PREFIX + id;

        // 检查是否已点赞
        Boolean isMember = redisTemplate.opsForSet().isMember(likeKey, userId.toString());
        if (Boolean.TRUE.equals(isMember)) {
            throw new BusinessException(ResultCode.COMMENT_ALREADY_LIKED);
        }

        // 添加点赞记录到Redis
        redisTemplate.opsForSet().add(likeKey, userId.toString());

        // 数据库点赞数+1
        petRescueMapper.incrementLikeCount(id);

        // 保存点赞记录到数据库
        PetLike petLike = new PetLike();
        petLike.setPetId(id);
        petLike.setUserId(userId);
        petLikeMapper.insert(petLike);

        log.info("用户{}点赞宠物{}", userId, id);
    }

    @Override
    public void unlikePet(Long id) {
        Long userId = UserHolder.getUser().getId();
        String likeKey = LIKE_KEY_PREFIX + id;

        // 检查是否已点赞
        Boolean isMember = redisTemplate.opsForSet().isMember(likeKey, userId.toString());
        if (!Boolean.TRUE.equals(isMember)) {
            throw new BusinessException(ResultCode.COMMENT_NOT_LIKED);
        }

        // 从Redis移除点赞
        redisTemplate.opsForSet().remove(likeKey, userId.toString());

        // 数据库点赞数-1
        petRescueMapper.decrementLikeCount(id);

        // 删除数据库点赞记录
        LambdaQueryWrapper<PetLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetLike::getPetId, id).eq(PetLike::getUserId, userId);
        petLikeMapper.delete(wrapper);

        log.info("用户{}取消点赞宠物{}", userId, id);
    }

    /**
     * 转换为VO
     */
    private PetRescueVO convertToVO(PetRescue pet) {
        PetRescueVO vo = BeanUtil.copyProperties(pet, PetRescueVO.class);

        // 设置宠物类型名称
        vo.setPetTypeName(PetType.getName(pet.getPetType()));

        // 设置状态名称
        vo.setStatusName(PetStatus.getName(pet.getStatus()));

        // 设置性别名称
        if (pet.getPetGender() != null) {
            vo.setPetGenderName(switch (pet.getPetGender()) {
                case 1 -> "公";
                case 2 -> "母";
                default -> "未知";
            });
        }

        // 解析图片URL列表
        if (StringUtils.hasText(pet.getImages())) {
            vo.setImageUrls(JSONUtil.toList(pet.getImages(), String.class));
        }

        // 查询发布者信息
        User publisher = userMapper.selectById(pet.getPublisherId());
        if (publisher != null) {
            vo.setPublisherName(publisher.getNickname());
            vo.setPublisherAvatar(publisher.getAvatar());
        }

        return vo;
    }
}