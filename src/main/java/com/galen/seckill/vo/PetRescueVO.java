package com.galen.seckill.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 宠物救助信息VO
 *
 * @author Galen
 * @since 2026-10-08
 */
@Data
public class PetRescueVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String petName;
    private Integer petType;
    private String petTypeName;
    private String petBreed;
    private Integer petGender;
    private String petGenderName;
    private String petAge;
    private String petColor;
    private BigDecimal petWeight;
    private String healthStatus;
    private Integer vaccinationStatus;
    private Integer sterilizationStatus;
    private String description;
    private String rescueLocation;
    private LocalDateTime rescueTime;
    private List<String> imageUrls;
    private String videoUrl;
    private Integer status;
    private String statusName;
    private Long publisherId;
    private String publisherName;
    private String publisherAvatar;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLiked;
    private LocalDateTime createTime;
}