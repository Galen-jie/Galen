package com.galen.seckill.service.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.galen.seckill.common.ResultCode;
import com.galen.seckill.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 阿里云OSS存储策略
 *
 * @author Galen
 * @since 2026-10-09
 */
@Slf4j
@Component
public class OssStorageStrategy implements StorageStrategy {

    @Value("${galen.upload.oss.endpoint}")
    private String endpoint;

    @Value("${galen.upload.oss.access-key-id}")
    private String accessKeyId;

    @Value("${galen.upload.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${galen.upload.oss.bucket-name}")
    private String bucketName;

    @Value("${galen.upload.oss.url-prefix}")
    private String urlPrefix;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            log.info("OSS客户端初始化成功, endpoint: {}, bucket: {}", endpoint, bucketName);
        } catch (Exception e) {
            log.error("OSS客户端初始化失败", e);
            throw new BusinessException(ResultCode.OSS_INIT_FAILED);
        }
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSS客户端已关闭");
        }
    }

    @Override
    public String upload(MultipartFile file, String businessType) {
        if (ossClient == null) {
            log.error("OSS客户端未初始化");
            throw new BusinessException(ResultCode.OSS_INIT_FAILED);
        }

        String originalFilename = file.getOriginalFilename();
        String fileName = generateFileName(originalFilename);
        String objectKey = getObjectKey(fileName, businessType);

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            metadata.setCacheControl("max-age=31536000");

            ossClient.putObject(bucketName, objectKey, inputStream, metadata);

            String fileUrl = urlPrefix + objectKey;
            log.info("OSS文件上传成功, 大小: {} bytes, 文件: {}", file.getSize(), fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("OSS文件读取失败: {}", originalFilename, e);
            throw new BusinessException(ResultCode.OSS_UPLOAD_FAILED);
        } catch (Exception e) {
            log.error("OSS上传异常, bucket: {}, key: {}", bucketName, objectKey, e);
            throw new BusinessException(ResultCode.OSS_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (ossClient == null) {
            log.error("OSS客户端未初始化");
            throw new BusinessException(ResultCode.OSS_INIT_FAILED);
        }

        try {
            String objectKey = fileUrl.replace(urlPrefix, "");
            ossClient.deleteObject(bucketName, objectKey);
            log.info("OSS文件删除成功: {}", fileUrl);
        } catch (Exception e) {
            log.error("OSS删除失败: {}", fileUrl, e);
            throw new BusinessException(ResultCode.OSS_DELETE_FAILED);
        }
    }

    @Override
    public String getType() {
        return "oss";
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private String getObjectKey(String fileName, String businessType) {
        LocalDate today = LocalDate.now();
        String datePath = String.format("%d/%02d/%02d",
                today.getYear(), today.getMonthValue(), today.getDayOfMonth());

        if (businessType != null && !businessType.isEmpty()) {
            return businessType + "/" + datePath + "/" + fileName;
        }
        return datePath + "/" + fileName;
    }
}