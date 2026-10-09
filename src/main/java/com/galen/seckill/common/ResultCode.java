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
    ORDER_STATUS_ERROR(3005, "订单状态错误"),

    // 宠物救助相关错误 4xxx
    PET_NOT_FOUND(4001, "宠物信息不存在"),
    PET_ALREADY_ADOPTED(4002, "该宠物已被领养"),
    PET_OFFLINE(4003, "该宠物已下架"),
    PET_NO_PERMISSION(4004, "无权限操作该宠物信息"),
    PET_IMAGE_LIMIT(4005, "图片数量超过限制"),

    // 领养申请相关错误 5xxx
    APPLICATION_NOT_FOUND(5001, "领养申请不存在"),
    APPLICATION_ALREADY_EXISTS(5002, "已提交过领养申请"),
    APPLICATION_ALREADY_PROCESSED(5003, "申请已处理，无法重复操作"),
    APPLICATION_NO_PERMISSION(5004, "无权限审核该申请"),

    // 评论点赞相关错误 6xxx
    COMMENT_NOT_FOUND(6001, "评论不存在"),
    COMMENT_ALREADY_LIKED(6002, "已点赞"),
    COMMENT_NOT_LIKED(6003, "未点赞，无法取消"),
    FILE_UPLOAD_FAILED(6004, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(6005, "文件类型不允许"),
    FILE_SIZE_EXCEEDED(6006, "文件大小超过限制"),

    // OSS存储相关错误 7xxx
    OSS_INIT_FAILED(7001, "OSS初始化失败"),
    OSS_UPLOAD_FAILED(7002, "OSS上传失败"),
    OSS_DELETE_FAILED(7003, "OSS删除失败"),
    OSS_CONFIG_ERROR(7004, "OSS配置错误");

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;
}