package com.galen.seckill.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 *
 * @author Galen
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    FAILED(400, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "资源不存在"),

    // 服务端错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    // 业务错误 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_ALREADY_EXISTS(1004, "用户已存在"),
    PHONE_ALREADY_EXISTS(1005, "手机号已注册"),
    EMAIL_ALREADY_EXISTS(1006, "邮箱已注册"),

    // 秒杀相关错误 2xxx
    SECKILL_NOT_FOUND(2001, "秒杀商品不存在"),
    SECKILL_NOT_STARTED(2002, "秒杀活动未开始"),
    SECKILL_ENDED(2003, "秒杀活动已结束"),
    SECKILL_STOCK_EMPTY(2004, "秒杀商品库存不足"),
    SECKILL_REPEAT_BUY(2005, "不能重复购买"),
    SECKILL_CAPTCHA_ERROR(2006, "验证码错误"),
    SECKILL_PATH_ERROR(2007, "秒杀路径错误"),
    SECKILL_ACCESS_LIMIT(2008, "访问过于频繁，请稍后再试"),

    // 订单相关错误 3xxx
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_ALREADY_PAID(3002, "订单已支付"),
    ORDER_ALREADY_CANCELLED(3003, "订单已取消"),
    ORDER_TIMEOUT(3004, "订单已超时"),
    ORDER_STATUS_ERROR(3005, "订单状态错误");

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;
}