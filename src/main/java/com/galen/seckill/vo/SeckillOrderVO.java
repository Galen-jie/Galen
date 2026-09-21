package com.galen.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillOrderVO {

    private Long id;

    private String orderNo;

    private Long userId;

    private String seckillId;

    private Long goodsId;

    private String goodsName;

    private String goodsImg;

    private BigDecimal orderPrice;

    private Integer status;

    private String statusText;

    private Integer payType;

    private String payTypeText;

    private LocalDateTime payTime;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}