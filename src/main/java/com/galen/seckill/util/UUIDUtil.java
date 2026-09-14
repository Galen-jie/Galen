package com.galen.seckill.util;

import java.util.UUID;

/**
 * UUID工具类
 *
 * @author Galen
 * @since 2024-01-01
 */
public class UUIDUtil {

    /**
     * 生成UUID（去掉横线）
     *
     * @return UUID字符串
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成UUID（保留横线）
     *
     * @return UUID字符串
     */
    public static String uuidWithDash() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成简短UUID（16位）
     *
     * @return UUID字符串
     */
    public static String shortUUID() {
        return uuid().substring(0, 16);
    }
}