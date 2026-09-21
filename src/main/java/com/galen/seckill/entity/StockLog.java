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
@TableName("stock_log")
public class StockLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String seckillId;

    private String orderNo;

    private Integer changeType;

    private Integer changeCount;

    private Integer beforeStock;

    private Integer afterStock;

    private String remark;

    private LocalDateTime createTime;
}