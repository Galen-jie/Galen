package com.galen.seckill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 宠物救助信息DTO
 *
 * @author Galen
 * @since 2026-10-08
 */
@Data
public class PetRescueDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 宠物名称
     */
    private String petName;

    /**
     * 宠物类型：1-狗，2-猫，3-其他
     */
    @NotNull(message = "宠物类型不能为空")
    private Integer petType;

    /**
     * 品种
     */
    private String petBreed;

    /**
     * 性别：0-未知，1-公，2-母
     */
    private Integer petGender;

    /**
     * 年龄
     */
    private String petAge;

    /**
     * 毛色
     */
    private String petColor;

    /**
     * 体重(kg)
     */
    private BigDecimal petWeight;

    /**
     * 健康状况
     */
    private String healthStatus;

    /**
     * 疫苗接种：0-未接种，1-已接种
     */
    private Integer vaccinationStatus;

    /**
     * 绝育状态：0-未绝育，1-已绝育
     */
    private Integer sterilizationStatus;

    /**
     * 详细描述
     */
    @NotBlank(message = "详细描述不能为空")
    private String description;

    /**
     * 救助地点
     */
    private String rescueLocation;

    /**
     * 救助时间
     */
    private LocalDateTime rescueTime;

    /**
     * 图片URL列表
     */
    private List<String> imageUrls;

    /**
     * 视频URL
     */
    private String videoUrl;
}