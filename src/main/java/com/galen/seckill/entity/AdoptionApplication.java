package com.galen.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 领养申请实体类
 *
 * @author Galen
 * @since 2026-10-08
 */
@Data
@TableName("adoption_application")
public class AdoptionApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申请ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 申请编号
     */
    private String applicationNo;

    /**
     * 宠物ID
     */
    private Long petId;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 申请人电话
     */
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
    private String reason;

    /**
     * 状态：0-待审核，1-已通过，2-已拒绝，3-已取消
     */
    private Integer status;

    /**
     * 拒绝理由
     */
    private String rejectReason;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}