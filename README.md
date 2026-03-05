# Spring AI 多厂商大模型集成平台

基于 Spring AI 的多厂商大模型集成与多模态数据处理项目，支持 **Qwen/DeepSeek/腾讯/豆包/OpenAI/NVIDIA** 等主流 AI 厂商，实现智能问答、报表解析、文本生成、RAG 检索增强等能力。

## 核心特性

### 🤖 多 AI 厂商支持 (适配器模式)

| 厂商 | 模型 | 多模态 | Embedding | 状态 |
|------|------|--------|-----------|------|
| 阿里云百炼 | Qwen-Max | ✅ | ✅ | 已实现 |
| DeepSeek | deepseek-chat | ❌ | ❌ | 已实现 |
| 腾讯混元 | hunyuan-lite | ⚠️ | ⚠️ | 已实现 |
| 字节豆包 | Doubao-pro | ✅ | ✅ | 已实现 |
| OpenAI | GPT-4-Turbo | ✅ | ✅ | 已实现 |
| NVIDIA | llama3-70b | ❌ | ✅ | 已实现 |
| Ollama | llama3 (本地) | ⚠️ | ⚠️ | 已实现 |

### 📊 多模态数据处理

- **PDF 报表解析**: 自动提取文本、表格、关键指标
- **Excel 数据分析**: 遍历工作表、提取数据、生成结论
- **图片 OCR 识别**: 支持中英文文字识别
- **图表智能分析**: 柱状图/折线图/饼图数据提取
- **可视化建议**: 自动推荐合适的图表类型
- **结论自动生成**: AI 生成专业分析报告

### 🔧 工程化能力

- **API 限流**: Resilience4j 速率限制
- **自动重试**: 失败请求智能重试
- **熔断保护**: 防止级联故障
- **日志埋点**: 完整的请求/响应日志
- **统一异常处理**: 标准化错误响应

### 📚 RAG 检索增强

- 文档向量化存储
- 相似度检索
- 知识库管理
- 场景化问答 (工地安全/工业规范)

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- Tesseract OCR (图片识别，可选)

### 2. 配置 API Key

**方式一：环境变量（推荐）**

```bash
# 设置默认厂商
export AI_DEFAULT_PROVIDER=alibaba

# 阿里云百炼 (Qwen)
export DASHSCOPE_API_KEY=sk-xxxxxxxxxxxxxxxx

# DeepSeek
export DEEPSEEK_API_KEY=sk-xxxxxxxxxxxxxxxx

# OpenAI
export OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxx

# 更多厂商配置见 docs/多 AI 厂商配置指南.md
```

**方式二：修改 application.yml**

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
      enabled: false
      api-key: sk-your-openai-api-key
      model: gpt-4-turbo
```

### 3. 启动项目

```bash
cd spring-ai-alibaba-demo
mvn spring-boot:run
```

### 4. 测试接口

服务地址：http://localhost:8080/api

#### 测试 AI 对话

```bash
# 简单对话
curl -X POST "http://localhost:8080/api/chat/simple?question=什么是高处作业"

# 指定厂商对话
curl -X POST "http://localhost:8080/api/ai/chat" \
  -H "Content-Type: application/json" \
  -d '{"prompt": "你好", "provider": "openai"}'

# 获取可用厂商列表
curl "http://localhost:8080/api/ai/providers"
```

#### 测试多模态报表解析

```bash
# 解析 Excel 报表（自动提取指标 + 生成结论）
curl -X POST "http://localhost:8080/api/multimodal/parse/excel" \
  -F "file=@/path/to/sales-report.xlsx"

# 解析 PDF 报表
curl -X POST "http://localhost:8080/api/multimodal/parse/pdf" \
  -F "file=@/path/to/annual-report.pdf"

# 图片 OCR 识别
curl -X POST "http://localhost:8080/api/multimodal/ocr" \
  -F "file=@/path/to/screenshot.png"

# 图表分析
curl -X POST "http://localhost:8080/api/multimodal/analyze/chart" \
  -F "file=@/path/to/bar-chart.png"
```

## API 接口一览

### AI 厂商管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/ai/providers` | GET | 获取可用厂商列表 |
| `/api/ai/providers/default` | GET/POST | 获取/设置默认厂商 |
| `/api/ai/chat` | POST | 统一对话接口 |
| `/api/ai/multimodal` | POST | 多模态对话 |
| `/api/ai/broadcast` | POST | 广播测试（同时调用所有厂商） |
| `/api/ai/compare` | POST | 厂商能力对比 |

### 智能问答

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/chat/simple` | POST | 简单问答 |
| `/api/chat` | POST | 智能问答（支持 RAG） |
| `/api/chat/construction` | POST | 工地安全问答 |
| `/api/chat/conversation/{id}` | POST | 多轮对话 |

### 多模态数据处理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/multimodal/parse` | POST | 智能解析报表（自动识别格式） |
| `/api/multimodal/parse/pdf` | POST | 解析 PDF 报表 |
| `/api/multimodal/parse/excel` | POST | 解析 Excel 报表 |
| `/api/multimodal/parse/image` | POST | 解析图片报表 |
| `/api/multimodal/ocr` | POST | OCR 文字识别 |
| `/api/multimodal/analyze/chart` | POST | 图表分析 |

### 文本生成

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/generate/report/work` | POST | 生成工作报告 |
| `/api/generate/report/safety` | POST | 生成安全检查报告 |
| `/api/generate/meeting-minutes` | POST | 生成会议纪要 |
| `/api/generate/technical-doc` | POST | 生成技术文档 |
| `/api/generate/polish` | POST | 内容润色 |
| `/api/generate/training` | POST | 生成培训材料 |

### 知识库管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/knowledge` | GET/POST | 获取/添加知识库文档 |
| `/api/knowledge/search` | GET | 搜索知识库 |
| `/api/knowledge/init/safety` | POST | 初始化安全知识库 |

## 项目结构

```
spring-ai-alibaba-demo/
├── src/main/java/com/example/ai/
│   ├── AiApplication.java              # 启动类
│   ├── adapter/                        # AI 厂商适配器
│   │   ├── AiProviderAdapter.java      # 适配器接口
│   │   ├── AiProviderAdapterManager.java # 适配器管理器
│   │   └── impl/                       # 各厂商实现
│   │       ├── AlibabaAiAdapter.java   # 阿里云
│   │       ├── DeepSeekAiAdapter.java  # DeepSeek
│   │       ├── OpenAiCompatibleAdapter.java # OpenAI 兼容
│   │       └── NvidiaAiAdapter.java    # NVIDIA
│   ├── config/                         # 配置类
│   ├── controller/                     # REST 接口
│   │   ├── AiProviderController.java   # AI 厂商管理
│   │   ├── ChatController.java         # 智能问答
│   │   ├── DocumentController.java     # 文档解析
│   │   ├── MultimodalDataController.java # 多模态数据
│   │   ├── TextGenerationController.java # 文本生成
│   │   └── KnowledgeController.java    # 知识库
│   ├── model/                          # 数据模型
│   ├── service/                        # 服务层
│   │   ├── impl/                       # 服务实现
│   │   └── *.java                      # 服务接口
│   └── util/                           # 工具类
├── src/main/resources/
│   └── application.yml                 # 配置文件
├── docs/                               # 文档
│   ├── API 测试指南.md
│   ├── 技术方案.md
│   └── 多 AI 厂商配置指南.md
└── pom.xml                             # Maven 配置
```

## 多模态处理流程

```
上传报表文件
    │
    ▼
┌─────────────────┐
│ 自动识别格式     │ PDF/Excel/Image
└─────────────────┘
    │
    ▼
┌─────────────────┐
│ 提取内容         │ 文本/表格/图片
└─────────────────┘
    │
    ▼
┌─────────────────┐
│ 提取关键指标     │ 正则+AI 提取
└─────────────────┘
    │
    ▼
┌─────────────────┐
│ 生成可视化建议   │ 推荐图表类型
└─────────────────┘
    │
    ▼
┌─────────────────┐
│ AI 生成分析结论  │ 专业报告
└─────────────────┘
    │
    ▼
返回完整响应（结论 + 指标 + 可视化建议）
```

## 适配器模式优势

1. **统一接口**: 所有厂商使用相同的调用方式
2. **灵活切换**: 运行时可切换 AI 厂商
3. **降级备份**: 某厂商故障时可切换到其他厂商
4. **易于扩展**: 新增厂商只需添加新适配器
5. **能力探测**: 自动识别厂商支持的能力

## 生产接入建议

1. **API Key 管理**: 使用密钥管理系统，不要硬编码
2. **限流配置**: 根据业务需求调整限流参数
3. **监控告警**: 集成 Prometheus 监控各厂商调用情况
4. **成本控制**: 设置月度预算告警
5. **数据缓存**: 高频问答缓存到 Redis
6. **异步处理**: 文档解析等耗时操作异步化

## 许可证

MIT License

## 相关文档

- [API 测试指南](docs/API 测试指南.md)
- [技术方案](docs/技术方案.md)
- [多 AI 厂商配置指南](docs/多 AI 厂商配置指南.md)
