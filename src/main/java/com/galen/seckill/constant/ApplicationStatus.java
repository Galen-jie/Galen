package com.galen.seckill.constant;

/**
 * 申请状态常量
 *
 * @author Galen
 * @since 2026-10-08
 */
public class ApplicationStatus {

    /**
     * 待审核
     */
    public static final int PENDING = 0;

    /**
     * 已通过
     */
    public static final int APPROVED = 1;

    /**
     * 已拒绝
     */
    public static final int REJECTED = 2;

    /**
     * 已取消
     */
    public static final int CANCELLED = 3;

    /**
     * 获取申请状态名称
     */
    public static String getName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case PENDING -> "待审核";
            case APPROVED -> "已通过";
            case REJECTED -> "已拒绝";
            case CANCELLED -> "已取消";
            default -> "未知";
        };
    }
}