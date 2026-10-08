package com.galen.seckill.mq.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class SeckillMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long goodsId;
    private String orderNo;
    private String seckillId;
    private Long seckillGoodsId;

}
