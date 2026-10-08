package com.galen.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件上传记录实体类
 *
 * @author Galen
 * @since 2026-10-08
 */
@Data
@TableName("file_upload")
public class FileUpload implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 上传者ID
     */
    private Long uploaderId;

    /**
     * 业务类型（pet_image、pet_video等）
     */
    private String businessType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}