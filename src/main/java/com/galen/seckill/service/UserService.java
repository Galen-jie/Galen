package com.galen.seckill.service;

import com.galen.seckill.dto.UserLoginDTO;
import com.galen.seckill.dto.UserRegisterDTO;
import com.galen.seckill.dto.UserUpdateDTO;
import com.galen.seckill.entity.User;
import com.galen.seckill.vo.UserVO;

/**
 * 用户服务接口
 *
 * @author Galen
 * @since 2024-01-01
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 用户信息
     */
    UserVO register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录token
     */
    String login(UserLoginDTO loginDTO);

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserById(Long userId);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param updateDTO 更新信息
     * @return 更新后的用户信息
     */
    UserVO updateUser(Long userId, UserUpdateDTO updateDTO);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户实体
     */
    User getUserByPhone(String phone);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    User getUserByUsername(String username);
}