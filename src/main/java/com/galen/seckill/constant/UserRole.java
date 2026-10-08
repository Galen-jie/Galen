package com.galen.seckill.constant;

/**
 * 用户角色常量
 *
 * @author Galen
 * @since 2026-10-08
 */
public class UserRole {

    /**
     * 普通用户
     */
    public static final int NORMAL = 0;

    /**
     * 救助站/志愿者
     */
    public static final int RESCUE_STATION = 1;

    /**
     * 获取角色名称
     */
    public static String getName(Integer role) {
        if (role == null) {
            return "普通用户";
        }
        return switch (role) {
            case NORMAL -> "普通用户";
            case RESCUE_STATION -> "救助站/志愿者";
            default -> "普通用户";
        };
    }
}