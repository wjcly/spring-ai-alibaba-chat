# 多 AI 厂商配置指南

## 概述

本项目使用**适配器模式**统一接入了多家 AI 厂商，包括：

| 厂商 | 模型示例 | 多模态 | Embedding | 官网 |
|------|----------|--------|-----------|------|
| 阿里云百炼 | Qwen-Max | ✅ | ✅ | https://www.aliyun.com/product/dashscope |
| DeepSeek | deepseek-chat | ❌ | ❌ | https://platform.deepseek.com/ |
| 腾讯混元 | hunyuan-lite | ⚠️ | ⚠️ | https://cloud.tencent.com/product/hunyuan |
| 字节豆包 | Doubao-pro | ✅ | ✅ | https://www.volcengine.com/product/doubao |
| OpenAI | GPT-4-Turbo | ✅ | ✅ | https://platform.openai.com/ |
| NVIDIA | llama3-70b | ❌ | ✅ | https://build.nvidia.com/ |
| Ollama | llama3 (本地) | ⚠️ | ⚠️ | https://ollama.ai/ |

## 环境变量配置

### 方式一：环境变量（推荐）

```bash
# 默认 AI 厂商
export AI_DEFAULT_PROVIDER=alibaba

# 阿里云百炼 (Qwen/通义千问)
export DASHSCOPE_API_KEY=sk-xxxxxxxxxxxxxxxx

# DeepSeek
export DEEPSEEK_API_KEY=sk-xxxxxxxxxxxxxxxx

# 腾讯混元
export TENCENT_API_KEY=xxxxxxxxxxxxxxxx

# 字节豆包
export DOUBAO_API_KEY=xxxxxxxxxxxxxxxx

# OpenAI (ChatGPT)
export OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxx

# NVIDIA
export NVIDIA_API_KEY=nvapi-xxxxxxxxxxxxxxxx
```

### 方式二：修改 application.yml

```yaml
ai:
  providers:
    default-provider: alibaba
    
    alibaba:
      enabled: true
      api-key: sk-your-alibaba-api-key
      model: qwen-max
    
    deepseek:
      enabled: true
      api-key: sk-your-deepseek-api-key
      model: deepseek-chat
    
    openai:
      enabled: true
      api-key: sk-your-openai-api-key
      model: gpt-4-turbo
```

## 各厂商 API Key 获取方式

### 1. 阿里云百炼 (Qwen)

1. 访问 https://www.aliyun.com/product/dashscope
2. 注册/登录阿里云账号
3. 开通百炼服务
4. 创建 API Key

**优点**: 
- 支持中文能力强
- 支持多模态 (Qwen-VL)
- 有免费额度

### 2. DeepSeek (深度求索)

1. 访问 https://platform.deepseek.com/
2. 注册账号
3. 在控制台创建 API Key

**优点**:
- 性价比高
- 代码能力强

### 3. 腾讯混元

1. 访问 https://cloud.tencent.com/product/hunyuan
2. 注册腾讯云账号
3. 开通混元服务
4. 获取 API Key

### 4. 字节豆包

1. 访问 https://www.volcengine.com/product/doubao
2. 注册火山引擎账号
3. 开通豆包服务
4. 获取 API Key

**优点**:
- 多模态能力强
- 适合内容创作

### 5. OpenAI (ChatGPT)

1. 访问 https://platform.openai.com/
2. 注册账号 (需要海外手机号)
3. 创建 API Key

**优点**:
- 能力最强
- 生态完善

**注意**: 需要科学上网或使用代理

### 6. NVIDIA NIM

1. 访问 https://build.nvidia.com/
2. 注册 NVIDIA 账号
3. 获取 API Key

**优点**:
- 模型种类多
- 有免费额度

### 7. Ollama (本地部署)

1. 下载安装 https://ollama.ai/
2. 运行 `ollama run llama3`

**优点**:
- 完全免费
- 数据本地
- 无网络延迟

## API 使用示例

### 1. 统一对话接口

```bash
# 使用默认厂商
curl -X POST "http://localhost:8080/api/ai/chat" \
  -H "Content-Type: application/json" \
  -d '{"prompt": "你好，请介绍一下自己"}'

# 指定厂商
curl -X POST "http://localhost:8080/api/ai/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "你好",
    "provider": "openai"
  }'
```

### 2. 多模态对话

```bash
# 图片 + 文本
curl -X POST "http://localhost:8080/api/ai/multimodal" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "请分析这张图片",
    "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
  }'
```

### 3. 广播测试 (同时调用所有厂商)

```bash
curl -X POST "http://localhost:8080/api/ai/broadcast?prompt=你好"
```

### 4. 厂商对比

```bash
curl -X POST "http://localhost:8080/api/ai/compare" \
  -H "Content-Type: application/json" \
  -d '{
    "providers": ["alibaba", "openai", "deepseek"],
    "defaultPrompt": "什么是人工智能？"
  }'
```

### 5. 多模态报表解析

```bash
# 解析 Excel 报表
curl -X POST "http://localhost:8080/api/multimodal/parse/excel" \
  -F "file=@/path/to/report.xlsx"

# 解析 PDF 报表
curl -X POST "http://localhost:8080/api/multimodal/parse/pdf" \
  -F "file=@/path/to/report.pdf"

# 解析图片报表 (OCR)
curl -X POST "http://localhost:8080/api/multimodal/parse/image" \
  -F "file=@/path/to/chart.png"
```

## 适配器模式架构

```
┌─────────────────────────────────────────────────────────┐
│              AiProviderAdapterManager                    │
│  - 统一管理所有适配器                                    │
│  - 路由请求到对应厂商                                    │
│  - 支持默认厂商切换                                       │
└─────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│AlibabaAdapter │   │DeepSeekAdapter│   │OpenAiAdapter  │
│               │   │               │   │               │
│- chat()       │   │- chat()       │   │- chat()       │
│- multimodal() │   │- multimodal() │   │- multimodal() │
│- embed()      │   │- embed()      │   │- embed()      │
│- analyzeImage()│  │- analyzeImage()│ │- analyzeImage()│
└───────────────┘   └───────────────┘   └───────────────┘
        │                     │                     │
        ▼                     ▼                     ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│  阿里云 API    │   │  DeepSeek API  │   │  OpenAI API   │
└───────────────┘   └───────────────┘   └───────────────┘
```

## 新增 AI 厂商

如需添加新的 AI 厂商，只需：

1. 创建新的适配器类：

```java
public class NewProviderAdapter implements AiProviderAdapter {
    @Override
    public AiProvider getProvider() {
        return AiProvider.NEW_PROVIDER;
    }
    
    @Override
    public String chat(String prompt) {
        // 实现调用逻辑
    }
    
    // 实现其他方法...
}
```

2. 在 `AiProviderAdapterManager` 中注册：

```java
if (properties.getNewProvider().isEnabled()) {
    adapters.put("newprovider", new NewProviderAdapter(...));
}
```

3. 在配置文件中添加配置：

```yaml
ai:
  providers:
    new-provider:
      enabled: true
      api-key: your-api-key
      model: model-name
```

## 常见问题

### Q: 为什么调用失败？
A: 检查以下几点：
1. API Key 是否正确
2. 厂商是否已启用 (`enabled: true`)
3. 网络是否通畅
4. 查看日志中的详细错误信息

### Q: 如何选择最佳厂商？
A: 根据场景选择：
- 中文问答：阿里云 Qwen、百度文心
- 代码生成：DeepSeek、OpenAI
- 多模态：阿里云 Qwen-VL、OpenAI GPT-4V
- 本地部署：Ollama

### Q: 如何实现负载均衡？
A: 可以：
1. 设置默认厂商
2. 在代码中实现轮询策略
3. 使用广播接口同时调用

### Q: 多模态支持情况？
A: 
- ✅ 完全支持：阿里云 Qwen-VL、OpenAI GPT-4V
- ⚠️ 部分支持：腾讯混元、豆包
- ❌ 不支持：DeepSeek、NVIDIA (纯文本)
