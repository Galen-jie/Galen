package com.galen.seckill.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoodsCategoryDTO {
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;
    @NotBlank(message = "父分类ID不能为空")
    private Long parentId;

    private int sort;

    @Min(value = 0, message = "状态值必须大于等于0")
    @Max(value = 1, message = "状态值必须小于等于1")
    private int status;
}
