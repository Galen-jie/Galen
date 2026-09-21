package com.galen.seckill.service.impl;

import com.galen.seckill.entity.StockLog;
import com.galen.seckill.mapper.StockLogMapper;
import com.galen.seckill.service.StockLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StockLogServiceImpl implements StockLogService {

    @Autowired
    private StockLogMapper stockLogMapper;

    @Override
    public void saveStockLog(StockLog stockLog) {
        stockLogMapper.insert(stockLog);
    }

    @Override
    public void logStockChange(String seckillId, String orderNo, Integer changeType, Integer changeCount, Integer beforeStock, Integer afterStock, String remark) {
        StockLog stockLog = new StockLog();
        stockLog.setSeckillId(seckillId);
        stockLog.setOrderNo(orderNo);
        stockLog.setChangeType(changeType);
        stockLog.setChangeCount(changeCount);
        stockLog.setBeforeStock(beforeStock);
        stockLog.setAfterStock(afterStock);
        stockLog.setRemark(remark);
        stockLog.setCreateTime(LocalDateTime.now());

        stockLogMapper.insert(stockLog);
    }
}