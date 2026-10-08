package com.galen.seckill.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 领养申请VO
 *
 * @author Galen
 * @since 2026-10-08
 */
@Data
public class AdoptionApplicationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String applicationNo;
    private Long petId;
    private String petName;
    private String petImage;
    private Long applicantId;
    private String applicantName;
    private String applicantPhone;
    private String applicantAddress;
    private String applicantJob;
    private String livingSituation;
    private String experience;
    private String reason;
    private Integer status;
    private String statusName;
    private String rejectReason;
    private Long reviewerId;
    private String reviewerName;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
}