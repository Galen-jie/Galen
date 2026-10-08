package com.galen.seckill.constant;

/**
 * 宠物类型常量
 *
 * @author Galen
 * @since 2026-10-08
 */
public class PetType {

    /**
     * 狗
     */
    public static final int DOG = 1;

    /**
     * 猫
     */
    public static final int CAT = 2;

    /**
     * 其他
     */
    public static final int OTHER = 3;

    /**
     * 获取宠物类型名称
     */
    public static String getName(Integer type) {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case DOG -> "狗";
            case CAT -> "猫";
            case OTHER -> "其他";
            default -> "未知";
        };
    }
}