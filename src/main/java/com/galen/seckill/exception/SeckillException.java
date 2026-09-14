package com.galen.seckill.exception;

import com.galen.seckill.common.ResultCode;
import lombok.Getter;

/**
 * 秒杀异常
 *
 * @author Galen
 * @since 2024-01-01
 */
@Getter
public class SeckillException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    public SeckillException(String message) {
        super(message);
        this.code = ResultCode.FAILED.getCode();
        this.message = message;
    }

    public SeckillException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public SeckillException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }
}