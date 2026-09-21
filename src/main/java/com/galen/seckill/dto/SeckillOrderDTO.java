package com.galen.seckill.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SeckillOrderDTO {

    @NotBlank(message = "秒杀ID不能为空")
    private String seckillId;

    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @NotBlank(message = "收货人姓名不能为空")
    @Size(max = 50, message = "收货人姓名长度不能超过50个字符")
    private String receiverName;

    @NotBlank(message = "收货人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    @Size(max = 500, message = "收货地址长度不能超过500个字符")
    private String receiverAddress;
}