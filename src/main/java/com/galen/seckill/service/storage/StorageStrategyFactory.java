package com.galen.seckill.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存储策略工厂
 *
 * @author Galen
 * @since 2026-10-09
 */
@Slf4j
@Component
public class StorageStrategyFactory {

    @Value("${galen.upload.type:local}")
    private String storageType;

    @Autowired
    private List<StorageStrategy> strategies;

    private Map<String, StorageStrategy> strategyMap;

    @PostConstruct
    public void init() {
        strategyMap = new HashMap<>();
        for (StorageStrategy strategy : strategies) {
            strategyMap.put(strategy.getType(), strategy);
            log.info("注册存储策略: {}", strategy.getType());
        }
    }

    /**
     * 获取当前配置的存储策略
     *
     * @return 存储策略
     */
    public StorageStrategy getStrategy() {
        StorageStrategy strategy = strategyMap.get(storageType);
        if (strategy == null) {
            log.warn("未找到存储类型: {}, 使用默认本地存储", storageType);
            return strategyMap.get("local");
        }
        return strategy;
    }

    /**
     * 获取指定类型的存储策略
     *
     * @param type 存储类型
     * @return 存储策略
     */
    public StorageStrategy getStrategy(String type) {
        return strategyMap.get(type);
    }
}