package com.galen.seckill.mq.message;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SeckillMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long seckillGoodsId;
    private String seckillId;
    private Long userId;
    private Long goodsId;
    private String orderNo;

    // 商品信息（请求端查一次带过来，消费者避免再 join Goods 表）
    private String goodsName;
    private String goodsImg;
    private BigDecimal orderPrice;

    // 收货信息（只能由请求端传）
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

}
