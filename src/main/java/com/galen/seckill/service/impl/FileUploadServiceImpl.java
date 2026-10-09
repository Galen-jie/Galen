package com.galen.seckill.service.impl;

import com.galen.seckill.common.ResultCode;
import com.galen.seckill.entity.FileUpload;
import com.galen.seckill.exception.BusinessException;
import com.galen.seckill.mapper.FileUploadMapper;
import com.galen.seckill.service.FileUploadService;
import com.galen.seckill.service.storage.StorageStrategy;
import com.galen.seckill.service.storage.StorageStrategyFactory;
import com.galen.seckill.util.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传服务实现类
 *
 * @author Galen
 * @since 2026-10-08
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${galen.upload.allowed-image-types}")
    private String allowedImageTypes;

    @Value("${galen.upload.allowed-video-types}")
    private String allowedVideoTypes;

    @Value("${galen.upload.max-size}")
    private Long maxSize;

    @Autowired
    private StorageStrategyFactory strategyFactory;

    @Autowired
    private FileUploadMapper fileUploadMapper;

    @Override
    public String uploadFile(MultipartFile file, String businessType) {
        // 校验文件
        validateFile(file);

        // 获取存储策略
        StorageStrategy strategy = strategyFactory.getStrategy();

        // 执行上传
        String fileUrl = strategy.upload(file, businessType);

        // 保存上传记录
        saveUploadRecord(file, fileUrl, businessType);

        return fileUrl;
    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files, String businessType) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = uploadFile(file, businessType);
            urls.add(url);
        }
        return urls;
    }

    @Override
    public void deleteFile(String fileUrl) {
        StorageStrategy strategy = strategyFactory.getStrategy();
        strategy.delete(fileUrl);

        log.info("文件删除成功: {}", fileUrl);
    }

    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file) {
        // 校验文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 校验文件大小
        if (file.getSize() > maxSize) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED);
        }

        // 校验文件类型
        String extension = getFileExtension(file.getOriginalFilename());
        List<String> allowedTypes = Arrays.asList(
                (allowedImageTypes + "," + allowedVideoTypes).split(",")
        );

        if (!allowedTypes.contains(extension.toLowerCase())) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_ALLOWED);
        }
    }

    /**
     * 保存上传记录
     */
    private void saveUploadRecord(MultipartFile file, String fileUrl, String businessType) {
        FileUpload upload = new FileUpload();
        upload.setFileName(file.getOriginalFilename());
        upload.setFilePath(fileUrl);
        upload.setFileUrl(fileUrl);
        upload.setFileSize(file.getSize());
        upload.setFileType(getFileExtension(file.getOriginalFilename()));
        upload.setUploaderId(UserHolder.getUser().getId());
        upload.setBusinessType(businessType);
        fileUploadMapper.insert(upload);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}