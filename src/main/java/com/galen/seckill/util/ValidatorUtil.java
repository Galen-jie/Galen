package com.galen.seckill.util;

import cn.hutool.core.util.StrUtil;
import com.galen.seckill.common.ResultCode;
import com.galen.seckill.exception.BusinessException;

import java.util.regex.Pattern;

/**
 * 参数校验工具类
 *
 * @author Galen
 * @since 2024-01-01
 */
public class ValidatorUtil {

    /**
     * 手机号正则表达式
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 邮箱正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");

    /**
     * 验证手机号
     *
     * @param phone 手机号
     * @return 是否有效
     */
    public static boolean isPhone(String phone) {
        return StrUtil.isNotBlank(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证邮箱
     *
     * @param email 邮箱
     * @return 是否有效
     */
    public static boolean isEmail(String email) {
        return StrUtil.isNotBlank(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证手机号（抛出异常）
     *
     * @param phone 手机号
     */
    public static void validatePhone(String phone) {
        if (!isPhone(phone)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "手机号格式不正确");
        }
    }

    /**
     * 验证邮箱（抛出异常）
     *
     * @param email 邮箱
     */
    public static void validateEmail(String email) {
        if (!isEmail(email)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "邮箱格式不正确");
        }
    }

    /**
     * 验证非空
     *
     * @param str     字符串
     * @param message 错误消息
     */
    public static void validateNotEmpty(String str, String message) {
        if (StrUtil.isBlank(str)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, message);
        }
    }
}