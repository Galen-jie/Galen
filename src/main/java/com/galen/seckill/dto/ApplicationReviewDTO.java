package com.galen.seckill.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 申请审核DTO
 *
 * @author Galen
 * @since 2026-10-08
 */
@Data
public class ApplicationReviewDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申请ID
     */
    @NotNull(message = "申请ID不能为空")
    private Long applicationId;

    /**
     * 审核状态：1-通过，2-拒绝
     */
    @NotNull(message = "审核状态不能为空")
    private Integer status;

    /**
     * 拒绝理由（拒绝时必填）
     */
    private String rejectReason;
}