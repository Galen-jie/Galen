package com.galen.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsCategoryVO {
    private Long id;

    private String categoryName;

    private Long parentId;

    private int sort;

    private int status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<GoodsCategoryVO> children;
}

