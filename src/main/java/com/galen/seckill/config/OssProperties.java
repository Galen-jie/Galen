package com.galen.seckill.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * OSS配置属性
 *
 * @author Galen
 * @since 2026-10-09
 */
@Data
@Validated
@ConfigurationProperties(prefix = "galen.upload.oss")
public class OssProperties {

    @NotBlank(message = "OSS endpoint不能为空")
    private String endpoint;

    @NotBlank(message = "AccessKeyId不能为空")
    private String accessKeyId;

    @NotBlank(message = "AccessKeySecret不能为空")
    private String accessKeySecret;

    @NotBlank(message = "BucketName不能为空")
    private String bucketName;

    @NotBlank(message = "URL前缀不能为空")
    private String urlPrefix;
}