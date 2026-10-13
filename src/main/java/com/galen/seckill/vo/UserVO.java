package com.galen.seckill.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息VO
 *
 * @author Galen
 * @since 2024-01-01
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号（脱敏）
     */
    private String phone;

    /**
     * 邮箱（脱敏）
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 性格：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 角色：0-普通用户，1-救助站/志愿者
     */
    private Integer role;
}