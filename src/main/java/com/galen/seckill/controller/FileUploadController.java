package com.galen.seckill.controller;

import com.galen.seckill.common.Result;
import com.galen.seckill.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传控制器
 *
 * @author Galen
 * @since 2026-10-08
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 上传单个文件
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file,
                                      @RequestParam(required = false) String businessType) {
        String url = fileUploadService.uploadFile(file, businessType);
        return Result.success(url);
    }

    /**
     * 批量上传文件
     */
    @PostMapping("/upload/batch")
    public Result<List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files,
                                             @RequestParam(required = false) String businessType) {
        List<String> urls = fileUploadService.uploadFiles(files, businessType);
        return Result.success(urls);
    }

    /**
     * 删除文件
     */
    @DeleteMapping
    public Result<Void> deleteFile(@RequestParam String fileUrl) {
        fileUploadService.deleteFile(fileUrl);
        return Result.success();
    }
}