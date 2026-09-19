package com.galen.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("goods_category")
public class GoodsCategory {


    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    private String categoryName;

    private Long parentId;

    private int sort;

    private int status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
