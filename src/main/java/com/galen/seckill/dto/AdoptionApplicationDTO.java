package com.galen.seckill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 领养申请DTO
 *
 * @author Galen
 * @since 2026-10-08
 */
@Data
public class AdoptionApplicationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 宠物ID
     */
    @NotNull(message = "宠物ID不能为空")
    private Long petId;

    /**
     * 申请人姓名
     */
    @NotBlank(message = "申请人姓名不能为空")
    private String applicantName;

    /**
     * 申请人电话
     */
    @NotBlank(message = "申请人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String applicantPhone;

    /**
     * 申请人地址
     */
    private String applicantAddress;

    /**
     * 职业
     */
    private String applicantJob;

    /**
     * 居住情况
     */
    private String livingSituation;

    /**
     * 养宠经验
     */
    private String experience;

    /**
     * 领养理由
     */
    @NotBlank(message = "领养理由不能为空")
    private String reason;
}