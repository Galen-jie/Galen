# OSS 配置指南

## 1. 创建阿里云 OSS Bucket

1. 登录阿里云控制台：https://oss.console.aliyun.com/
2. 创建 Bucket：
   - Bucket 名称：`galen-petrescue`（或自定义）
   - 地域：选择离你最近的区域（如：华东1-杭州）
   - 存储类型：标准存储
   - 读写权限：公共读（重要！否则图片无法访问）
   - 其他选项使用默认值

3. 记录 Bucket 信息：
   - Endpoint：`oss-cn-beijing.aliyuncs.com`（根据实际地域修改）
   - Bucket 名称：`galen-petrescue`

## 2. 获取 AccessKey

1. 点击右上角头像 -> AccessKey 管理
2. 建议使用 RAM 子用户：
   - 创建 RAM 用户
   - 授予 `AliyunOSSFullAccess` 权限
   - 创建 AccessKey
3. 记录 AccessKey ID 和 AccessKey Secret

## 3. 配置环境变量

### Windows:
```bash
# 设置环境变量（临时，仅在当前命令窗口有效）
set ALIYUN_ACCESS_KEY_ID=你的AccessKeyID
set ALIYUN_ACCESS_KEY_SECRET=你的AccessKeySecret

# 或者永久设置（系统属性 -> 高级 -> 环境变量）
```

### Linux/Mac:
```bash
# 添加到 ~/.bashrc 或 ~/.zshrc
export ALIYUN_ACCESS_KEY_ID="你的AccessKeyID"
export ALIYUN_ACCESS_KEY_SECRET="你的AccessKeySecret"

# 立即生效
source ~/.bashrc
```

## 4. 修改配置文件

编辑 `src/main/resources/application-dev.yml`，根据你的实际情况修改：

```yaml
galen:
  upload:
    type: oss
    oss:
      endpoint: oss-cn-beijing.aliyuncs.com  # 修改为你的 endpoint
      access-key-id: ${ALIYUN_ACCESS_KEY_ID}
      access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
      bucket-name: galen-petrescue  # 修改为你的 bucket 名称
      url-prefix: https://galen-petrescue.oss-cn-beijing.aliyuncs.com/
```

## 5. 重要配置说明

### Endpoint 格式
- 根据你选择的地域，endpoint 格式为：`oss-cn-{region}.aliyuncs.com`
- 常见地域：
  - 华东1（杭州）：`oss-cn-hangzhou.aliyuncs.com`
  - 华东2（上海）：`oss-cn-shanghai.aliyuncs.com`
  - 华北2（北京）：`oss-cn-beijing.aliyuncs.com`
  - 华南1（深圳）：`oss-cn-shenzhen.aliyuncs.com`

### URL Prefix
- 格式：`https://{bucket-name}.{endpoint}/`
- 例如：`https://galen-petrescue.oss-cn-beijing.aliyuncs.com/`

### Bucket 权限
- 必须设置为**公共读**，否则前端无法访问图片
- 设置方法：Bucket 列表 -> 选择 Bucket -> 权限管理 -> 读写权限 -> 公共读

## 6. 验证配置

启动项目后，上传文件会看到日志：
```
OSS文件上传成功, 大小: xxx bytes, 文件: https://galen-petrescue.oss-cn-beijing.aliyuncs.com/...
```

## 7. 费用说明

阿里云 OSS 收费项目：
- 存储费用：约 0.12元/GB/月
- 流量费用：外网流出流量约 0.5元/GB
- 请求费用：PUT/GET 请求约 0.01元/万次

建议：
- 使用 CDN 加速可降低流量费用
- 定期清理无用文件
- 新用户有免费额度，可以查看阿里云官网活动

## 8. 故障排查

### 上传失败
1. 检查 AccessKey 是否正确
2. 检查 Bucket 权限是否为公共读
3. 检查 endpoint 是否正确

### 图片无法访问
1. 检查 URL prefix 是否正确
2. 检查 Bucket 读写权限
3. 在浏览器直接访问图片 URL 测试

### 客户端初始化失败
1. 检查网络连接
2. 检查 AccessKey 权限
3. 查看后端日志详细错误信息