package com.galen.seckill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsDTO {

    @NotBlank(message = "商品名称不能为空")
    private String goodsName;

    private String goodsTitle;

    private String goodsImg;

    private String goodsDetail;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal goodsPrice;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @Min(value = 0, message = "状态值不合法")
    @jakarta.validation.constraints.Max(value = 1, message = "状态值不合法")
    private Integer status;
}