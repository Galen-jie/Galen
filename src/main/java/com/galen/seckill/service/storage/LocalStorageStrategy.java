package com.galen.seckill.service.storage;

import com.galen.seckill.common.ResultCode;
import com.galen.seckill.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

/**
 * 本地文件存储策略
 *
 * @author Galen
 * @since 2026-10-09
 */
@Slf4j
@Component
public class LocalStorageStrategy implements StorageStrategy {

    @Value("${galen.upload.local-path}")
    private String localPath;

    @Value("${galen.upload.url-prefix}")
    private String urlPrefix;

    @Override
    public String upload(MultipartFile file, String businessType) {
        String originalFilename = file.getOriginalFilename();
        String fileName = generateFileName(originalFilename);
        String datePath = getDatePath();
        String relativePath = datePath + fileName;
        String fullPath = localPath + "/" + relativePath;

        File destFile = new File(fullPath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        try {
            file.transferTo(destFile);
            log.info("本地文件上传成功: {}", fullPath);
            return urlPrefix + relativePath;
        } catch (IOException e) {
            log.error("文件保存失败: {}", originalFilename, e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String fileUrl) {
        String relativePath = fileUrl.replace(urlPrefix, "");
        String fullPath = localPath + "/" + relativePath;

        File file = new File(fullPath);
        if (file.exists()) {
            if (file.delete()) {
                log.info("本地文件删除成功: {}", fileUrl);
            } else {
                log.warn("本地文件删除失败: {}", fileUrl);
            }
        } else {
            log.warn("文件不存在: {}", fileUrl);
        }
    }

    @Override
    public String getType() {
        return "local";
    }

    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return System.currentTimeMillis() + "_" + (int) (Math.random() * 10000) + "." + extension;
    }

    private String getDatePath() {
        LocalDate today = LocalDate.now();
        return String.format("%d/%02d/%02d/",
                today.getYear(), today.getMonthValue(), today.getDayOfMonth());
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}