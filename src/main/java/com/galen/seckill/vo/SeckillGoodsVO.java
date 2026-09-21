package com.galen.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillGoodsVO {

    private Long id;

    private String seckillId;

    private Long goodsId;

    private String goodsName;

    private String goodsTitle;

    private String goodsImg;

    private BigDecimal goodsPrice;

    private BigDecimal seckillPrice;

    private Integer stockCount;

    private Integer originalStockCount;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String statusText;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}