package com.galen.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.galen.seckill.entity.FileUpload;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件上传记录Mapper接口
 *
 * @author Galen
 * @since 2026-10-08
 */
@Mapper
public interface FileUploadMapper extends BaseMapper<FileUpload> {
}