package com.galen.seckill.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传服务接口
 *
 * @author Galen
 * @since 2026-10-08
 */
public interface FileUploadService {

    /**
     * 上传单个文件
     *
     * @param file 文件
     * @param businessType 业务类型
     * @return 文件URL
     */
    String uploadFile(MultipartFile file, String businessType);

    /**
     * 批量上传文件
     *
     * @param files 文件数组
     * @param businessType 业务类型
     * @return 文件URL列表
     */
    List<String> uploadFiles(MultipartFile[] files, String businessType);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);
}