package com.galen.seckill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.galen.seckill.common.PageResult;
import com.galen.seckill.common.ResultCode;
import com.galen.seckill.dto.PetCommentDTO;
import com.galen.seckill.entity.CommentLike;
import com.galen.seckill.entity.PetComment;
import com.galen.seckill.entity.User;
import com.galen.seckill.exception.BusinessException;
import com.galen.seckill.mapper.CommentLikeMapper;
import com.galen.seckill.mapper.PetCommentMapper;
import com.galen.seckill.mapper.PetRescueMapper;
import com.galen.seckill.mapper.UserMapper;
import com.galen.seckill.service.PetCommentService;
import com.galen.seckill.util.UserHolder;
import com.galen.seckill.vo.PetCommentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 宠物评论服务实现类
 *
 * @author Galen
 * @since 2026-10-08
 */
@Slf4j
@Service
public class PetCommentServiceImpl implements PetCommentService {

    @Autowired
    private PetCommentMapper petCommentMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Autowired
    private PetRescueMapper petRescueMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String COMMENT_LIKE_KEY_PREFIX = "galen:comment:like:";

    @Override
    @Transactional
    public PetCommentVO createComment(PetCommentDTO commentDTO) {
        Long userId = UserHolder.getUser().getId();

        // 创建评论
        PetComment comment = BeanUtil.copyProperties(commentDTO, PetComment.class);
        comment.setUserId(userId);
        comment.setLikeCount(0);
        comment.setStatus(1);
        petCommentMapper.insert(comment);

        // 更新宠物评论数
        petRescueMapper.incrementCommentCount(commentDTO.getPetId());

        log.info("用户{}发表评论: {}", userId, comment.getId());

        return convertToVO(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        Long userId = UserHolder.getUser().getId();

        // 查询评论
        PetComment comment = petCommentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }

        // 检查权限
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 软删除评论
        comment.setStatus(0);
        petCommentMapper.updateById(comment);

        // 更新宠物评论数
        petRescueMapper.decrementCommentCount(comment.getPetId());

        log.info("用户{}删除评论: {}", userId, id);
    }

    @Override
    public PageResult<PetCommentVO> getCommentList(Long petId, int pageNum, int pageSize) {
        Long userId = UserHolder.getUser().getId();

        // 查询一级评论
        LambdaQueryWrapper<PetComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetComment::getPetId, petId)
                .eq(PetComment::getStatus, 1)
                .eq(PetComment::getParentId, 0)
                .orderByDesc(PetComment::getCreateTime);

        // 分页查询
        Page<PetComment> page = new Page<>(pageNum, pageSize);
        Page<PetComment> result = petCommentMapper.selectPage(page, wrapper);

        // 转换为VO
        List<PetCommentVO> voList = result.getRecords().stream()
                .map(comment -> {
                    PetCommentVO vo = convertToVO(comment);
                    // 查询子评论
                    vo.setChildren(getChildComments(comment.getId(), userId));
                    return vo;
                })
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<PetCommentVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setPages(result.getPages());
        pageResult.setRecords(voList);

        return pageResult;
    }

    @Override
    @Transactional
    public void likeComment(Long id) {
        Long userId = UserHolder.getUser().getId();
        String likeKey = COMMENT_LIKE_KEY_PREFIX + id;

        // 检查评论是否存在
        PetComment comment = petCommentMapper.selectById(id);
        if (comment == null || comment.getStatus() == 0) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }

        // 检查是否已点赞
        Boolean isMember = redisTemplate.opsForSet().isMember(likeKey, userId.toString());
        if (Boolean.TRUE.equals(isMember)) {
            throw new BusinessException(ResultCode.COMMENT_ALREADY_LIKED);
        }

        // 添加点赞记录到Redis
        redisTemplate.opsForSet().add(likeKey, userId.toString());

        // 数据库点赞数+1
        petCommentMapper.incrementLikeCount(id);

        // 保存点赞记录到数据库
        CommentLike commentLike = new CommentLike();
        commentLike.setCommentId(id);
        commentLike.setUserId(userId);
        commentLikeMapper.insert(commentLike);

        log.info("用户{}点赞评论{}", userId, id);
    }

    @Override
    @Transactional
    public void unlikeComment(Long id) {
        Long userId = UserHolder.getUser().getId();
        String likeKey = COMMENT_LIKE_KEY_PREFIX + id;

        // 检查是否已点赞
        Boolean isMember = redisTemplate.opsForSet().isMember(likeKey, userId.toString());
        if (!Boolean.TRUE.equals(isMember)) {
            throw new BusinessException(ResultCode.COMMENT_NOT_LIKED);
        }

        // 从Redis移除点赞
        redisTemplate.opsForSet().remove(likeKey, userId.toString());

        // 数据库点赞数-1
        petCommentMapper.decrementLikeCount(id);

        // 删除数据库点赞记录
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getCommentId, id).eq(CommentLike::getUserId, userId);
        commentLikeMapper.delete(wrapper);

        log.info("用户{}取消点赞评论{}", userId, id);
    }

    /**
     * 获取子评论
     */
    private List<PetCommentVO> getChildComments(Long parentId, Long userId) {
        LambdaQueryWrapper<PetComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetComment::getParentId, parentId)
                .eq(PetComment::getStatus, 1)
                .orderByAsc(PetComment::getCreateTime);

        List<PetComment> children = petCommentMapper.selectList(wrapper);
        return children.stream()
                .map(comment -> convertToVO(comment))
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO
     */
    private PetCommentVO convertToVO(PetComment comment) {
        Long userId = UserHolder.getUser().getId();
        PetCommentVO vo = BeanUtil.copyProperties(comment, PetCommentVO.class);

        // 查询评论用户信息
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setUserName(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }

        // 查询回复用户信息
        if (comment.getReplyUserId() != null) {
            User replyUser = userMapper.selectById(comment.getReplyUserId());
            if (replyUser != null) {
                vo.setReplyUserName(replyUser.getNickname());
            }
        }

        // 检查是否点赞
        String likeKey = COMMENT_LIKE_KEY_PREFIX + comment.getId();
        Boolean isLiked = redisTemplate.opsForSet().isMember(likeKey, userId.toString());
        vo.setIsLiked(Boolean.TRUE.equals(isLiked));

        return vo;
    }
}