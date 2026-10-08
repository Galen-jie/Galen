package com.galen.seckill.constant;

/**
 * 宠物状态常量
 *
 * @author Galen
 * @since 2026-10-08
 */
public class PetStatus {

    /**
     * 待领养
     */
    public static final int WAITING = 0;

    /**
     * 已预约
     */
    public static final int RESERVED = 1;

    /**
     * 已领养
     */
    public static final int ADOPTED = 2;

    /**
     * 已下架
     */
    public static final int OFFLINE = 3;

    /**
     * 获取宠物状态名称
     */
    public static String getName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case WAITING -> "待领养";
            case RESERVED -> "已预约";
            case ADOPTED -> "已领养";
            case OFFLINE -> "已下架";
            default -> "未知";
        };
    }
}