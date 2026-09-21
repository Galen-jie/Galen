package com.galen.seckill.service;

import com.galen.seckill.entity.StockLog;

public interface StockLogService {

    void saveStockLog(StockLog stockLog);

    void logStockChange(String seckillId, String orderNo, Integer changeType, Integer changeCount, Integer beforeStock, Integer afterStock, String remark);
}