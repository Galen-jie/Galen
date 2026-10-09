package com.galen.seckill.service.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储策略接口
 *
 * @author Galen
 * @since 2026-10-09
 */
public interface StorageStrategy {

    /**
     * 上传文件
     *
     * @param file         文件
     * @param businessType 业务类型
     * @return 文件访问URL
     */
    String upload(MultipartFile file, String businessType);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     */
    void delete(String fileUrl);

    /**
     * 获取存储类型标识
     *
     * @return 存储类型
     */
    String getType();
}