package com.galen.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.galen.seckill.common.ResultCode;
import com.galen.seckill.dto.UserLoginDTO;
import com.galen.seckill.dto.UserRegisterDTO;
import com.galen.seckill.dto.UserUpdateDTO;
import com.galen.seckill.entity.User;
import com.galen.seckill.exception.BusinessException;
import com.galen.seckill.mapper.UserMapper;
import com.galen.seckill.service.UserService;
import com.galen.seckill.util.MD5Util;
import com.galen.seckill.util.UUIDUtil;
import com.galen.seckill.util.ValidatorUtil;
import com.galen.seckill.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 *
 * @author Galen
 * @since 2024-01-01
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final String SESSION_PREFIX = "galen:session:";
    private static final long SESSION_EXPIRE_TIME = 30 * 60; // 30分钟

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserRegisterDTO registerDTO) {
        // 验证手机号格式
        if (!ValidatorUtil.isPhone(registerDTO.getPhone())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "手机号格式不正确");
        }

        // 验证用户名是否已存在
        User existUser = userMapper.selectByUsername(registerDTO.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        // 验证手机号是否已注册
        existUser = userMapper.selectByPhone(registerDTO.getPhone());
        if (existUser != null) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_EXISTS);
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPhone(registerDTO.getPhone());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname());
        user.setEmail(registerDTO.getEmail());
        user.setStatus(1);
        user.setGender(0);

        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "注册失败");
        }

        log.info("用户注册成功: {}", user.getUsername());

        return convertToUserVO(user);
    }

    @Override
    public String login(UserLoginDTO loginDTO) {
        // 根据登录方式查询用户
        User user = null;
        if (StringUtils.hasText(loginDTO.getUsername())) {
            user = userMapper.selectByUsername(loginDTO.getUsername());
        } else if (StringUtils.hasText(loginDTO.getPhone())) {
            user = userMapper.selectByPhone(loginDTO.getPhone());
        } else {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "请输入用户名或手机号");
        }

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 生成session token
        String token = UUIDUtil.uuid();
        String sessionKey = SESSION_PREFIX + token;

        // 存储用户信息到Redis
        stringRedisTemplate.opsForValue().set(sessionKey, user.getId().toString(), SESSION_EXPIRE_TIME, TimeUnit.SECONDS);

        log.info("用户登录成功: {}, token: {}", user.getUsername(), token);

        return token;
    }

    @Override
    public UserVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return convertToUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUser(Long userId, UserUpdateDTO updateDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 更新用户信息
        if (StringUtils.hasText(updateDTO.getNickname())) {
            user.setNickname(updateDTO.getNickname());
        }
        if (StringUtils.hasText(updateDTO.getEmail())) {
            user.setEmail(updateDTO.getEmail());
        }
        if (StringUtils.hasText(updateDTO.getAvatar())) {
            user.setAvatar(updateDTO.getAvatar());
        }
        if (updateDTO.getGender() != null) {
            user.setGender(updateDTO.getGender());
        }

        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "更新失败");
        }

        log.info("用户信息更新成功: {}", user.getUsername());

        return convertToUserVO(user);
    }

    @Override
    public User getUserByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 转换为UserVO
     */
    private UserVO convertToUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
}