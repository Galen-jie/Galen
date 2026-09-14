package com.galen.seckill.util;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * MD5工具类
 *
 * @author Galen
 * @since 2024-01-01
 */
public class MD5Util {

    /**
     * MD5加密
     *
     * @param str 待加密字符串
     * @return 加密后的字符串
     */
    public static String md5(String str) {
        return DigestUtil.md5Hex(str);
    }

    /**
     * MD5加密（带盐）
     *
     * @param str  待加密字符串
     * @param salt 盐值
     * @return 加密后的字符串
     */
    public static String md5(String str, String salt) {
        return DigestUtil.md5Hex(str + salt);
    }

    /**
     * 验证MD5
     *
     * @param str          待验证字符串
     * @param md5Str       MD5字符串
     * @return 是否匹配
     */
    public static boolean verify(String str, String md5Str) {
        return md5Str.equals(md5(str));
    }

    /**
     * 验证MD5（带盐）
     *
     * @param str          待验证字符串
     * @param salt         盐值
     * @param md5Str       MD5字符串
     * @return 是否匹配
     */
    public static boolean verify(String str, String salt, String md5Str) {
        return md5Str.equals(md5(str, salt));
    }
}