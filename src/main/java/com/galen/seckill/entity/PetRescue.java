package com.galen.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宠物救助信息实体类
 *
 * @author Galen
 * @since 2026-10-08
 */
@Data
@TableName("pet_rescue")
public class PetRescue implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 宠物ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 宠物名称
     */
    private String petName;

    /**
     * 宠物类型：1-狗，2-猫，3-其他
     */
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
     * 图片URL列表（JSON数组）
     */
    private String images;

    /**
     * 视频URL
     */
    private String video;

    /**
     * 状态：0-待领养，1-已预约，2-已领养，3-已下架
     */
    private Integer status;

    /**
     * 发布者ID
     */
    private Long publisherId;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}