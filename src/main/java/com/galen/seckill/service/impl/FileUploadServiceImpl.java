package com.galen.seckill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.galen.seckill.common.ResultCode;
import com.galen.seckill.entity.FileUpload;
import com.galen.seckill.exception.BusinessException;
import com.galen.seckill.mapper.FileUploadMapper;
import com.galen.seckill.service.FileUploadService;
import com.galen.seckill.util.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
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

    @Value("${galen.upload.type:local}")
    private String uploadType;

    @Value("${galen.upload.local-path}")
    private String localPath;

    @Value("${galen.upload.url-prefix}")
    private String urlPrefix;

    @Value("${galen.upload.allowed-image-types}")
    private String allowedImageTypes;

    @Value("${galen.upload.allowed-video-types}")
    private String allowedVideoTypes;

    @Value("${galen.upload.max-size}")
    private Long maxSize;

    @Autowired
    private FileUploadMapper fileUploadMapper;

    @Override
    public String uploadFile(MultipartFile file, String businessType) {
        // 校验文件
        validateFile(file);

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        // 生成唯一文件名
        String fileName = System.currentTimeMillis() + "_" + (int)(Math.random() * 10000) + "." + extension;

        // 按日期分目录
        String datePath = getDatePath();
        String relativePath = datePath + fileName;
        String fullPath = localPath + "/" + relativePath;

        // 创建目录
        File destFile = new File(fullPath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        // 保存文件
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("文件保存失败: {}", originalFilename, e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED);
        }

        // 生成访问URL
        String fileUrl = urlPrefix + relativePath;

        // 保存上传记录
        FileUpload upload = new FileUpload();
        upload.setFileName(originalFilename);
        upload.setFilePath(fullPath);
        upload.setFileUrl(fileUrl);
        upload.setFileSize(file.getSize());
        upload.setFileType(extension);
        upload.setUploaderId(UserHolder.getUser().getId());
        upload.setBusinessType(businessType);
        fileUploadMapper.insert(upload);

        log.info("文件上传成功: {}", fileUrl);
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
        // 从URL中提取相对路径
        String relativePath = fileUrl.replace(urlPrefix, "");
        String fullPath = localPath + "/" + relativePath;

        // 删除文件
        File file = new File(fullPath);
        if (file.exists()) {
            file.delete();
            log.info("文件删除成功: {}", fileUrl);
        } else {
            log.warn("文件不存在: {}", fileUrl);
        }
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
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 获取日期路径
     */
    private String getDatePath() {
        LocalDate today = LocalDate.now();
        return String.format("%d/%02d/%02d/",
                today.getYear(), today.getMonthValue(), today.getDayOfMonth());
    }
}