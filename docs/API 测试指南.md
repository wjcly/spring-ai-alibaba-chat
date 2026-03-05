# Spring AI Alibaba Demo API 测试指南

## 测试准备

1. 启动项目：`mvn spring-boot:run`
2. 确保阿里云百炼 API Key 已配置
3. 使用 Postman 或 curl 进行测试

## API 接口测试

### 1. 智能问答接口

#### 1.1 简单问答
```bash
curl -X POST "http://localhost:8080/api/chat/simple?question=什么是高处作业"
```

预期响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "answer": "高处作业是指在坠落高度基准面 2 米及以上有可能坠落的高处进行的作业...",
    "conversationId": "xxx",
    "model": "qwen-max",
    "durationMs": 1234
  },
  "requestId": "xxx"
}
```

#### 1.2 带 RAG 的智能问答
```bash
curl -X POST "http://localhost:8080/api/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "脚手架搭设有哪些安全要求？",
    "useRag": true,
    "scene": "工地安全",
    "temperature": 0.7
  }'
```

#### 1.3 多轮对话
```bash
# 第一轮
curl -X POST "http://localhost:8080/api/chat/conversation/test123?question=安全帽如何正确佩戴"

# 第二轮 (使用相同 conversationId)
curl -X POST "http://localhost:8080/api/chat/conversation/test123?question=那安全带呢"
```

#### 1.4 工地安全专用问答
```bash
curl -X POST "http://localhost:8080/api/chat/construction?question=深基坑开挖的安全原则是什么"
```

### 2. 文档解析接口

#### 2.1 上传并解析文档
```bash
curl -X POST "http://localhost:8080/api/document/parse" \
  -F "file=@/path/to/test.pdf" \
  -F "extractTables=true"
```

#### 2.2 文档总结
```bash
curl -X POST "http://localhost:8080/api/document/summarize" \
  -F "file=@/path/to/report.docx"
```

#### 2.3 提取表格
```bash
curl -X POST "http://localhost:8080/api/document/extract/tables" \
  -F "file=@/path/to/data.xlsx"
```

### 3. 文本生成接口

#### 3.1 生成工作报告
```bash
curl -X POST "http://localhost:8080/api/generate/report/work?type=周报" \
  -H "Content-Type: application/json" \
  -d '{
    "completedWork": "1. 完成 A 区脚手架搭设 2. 完成 B 区基础浇筑",
    "ongoingWork": "C 区钢筋绑扎",
    "problems": "近期雨水较多，影响施工进度",
    "plan": "下周计划完成 C 区混凝土浇筑"
  }'
```

#### 3.2 生成安全检查报告
```bash
curl -X POST "http://localhost:8080/api/generate/report/safety?scene=工地" \
  -H "Content-Type: application/json" \
  -d '{
    "inspectionArea": "1 号楼、2 号楼施工现场",
    "findings": "1. 部分工人未正确佩戴安全帽 2. 脚手架连墙件设置不足",
    "hazards": "1. 临边防护缺失 2. 临时用电不规范",
    "rectifications": "1. 立即整改临边防护 2. 3 日内完成脚手架加固"
  }'
```

#### 3.3 生成会议纪要
```bash
curl -X POST "http://localhost:8080/api/generate/meeting-minutes" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "会议讨论了施工进度问题，决定增加施工人员，加快材料采购",
    "participants": ["张三", "李四", "王五"]
  }'
```

#### 3.4 内容润色
```bash
curl -X POST "http://localhost:8080/api/generate/polish?style=正式" \
  -H "Content-Type: text/plain" \
  -d '今天工地干活的人有点少，进度有点慢，要加点人'
```

### 4. 知识库管理接口

#### 4.1 初始化安全知识库
```bash
curl -X POST "http://localhost:8080/api/knowledge/init/safety"
```

#### 4.2 添加文档到知识库
```bash
curl -X POST "http://localhost:8080/api/knowledge" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "塔吊操作安全规程",
    "content": "塔吊操作人员必须持证上岗，作业前应检查各安全装置是否正常...",
    "docType": "规范",
    "scene": "工地安全",
    "keywords": "塔吊，起重机械，安全操作"
  }'
```

#### 4.3 获取知识库列表
```bash
# 获取所有文档
curl -X GET "http://localhost:8080/api/knowledge"

# 按场景筛选
curl -X GET "http://localhost:8080/api/knowledge?scene=工地安全"

# 按类型筛选
curl -X GET "http://localhost:8080/api/knowledge?docType=规范"
```

#### 4.4 搜索知识库
```bash
curl -X GET "http://localhost:8080/api/knowledge/search?keyword=脚手架"
```

#### 4.5 删除文档
```bash
curl -X DELETE "http://localhost:8080/api/knowledge/1"
```

## 错误响应示例

### 参数校验失败 (400)
```json
{
  "code": 400,
  "message": "参数校验失败",
  "data": {
    "question": "问题不能为空"
  }
}
```

### 请求频率过高 (429)
```json
{
  "code": 429,
  "message": "请求频率过高，请稍后重试"
}
```

### 服务器错误 (500)
```json
{
  "code": 500,
  "message": "服务器内部错误：xxx"
}
```

## Postman 集合

导入以下 Postman 集合可快速测试：

```json
{
  "info": {
    "name": "Spring AI Alibaba Demo",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "智能问答",
      "item": [
        {
          "name": "简单问答",
          "request": {
            "method": "POST",
            "url": "http://localhost:8080/api/chat/simple?question=什么是高处作业"
          }
        },
        {
          "name": "RAG 问答",
          "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {
              "mode": "raw",
              "raw": "{\"question\":\"脚手架搭设要求\",\"useRag\":true,\"scene\":\"工地安全\"}"
            },
            "url": "http://localhost:8080/api/chat"
          }
        }
      ]
    },
    {
      "name": "文本生成",
      "item": [
        {
          "name": "生成工作报告",
          "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {
              "mode": "raw",
              "raw": "{\"completedWork\":\"完成 A 区施工\",\"ongoingWork\":\"B 区基础\",\"problems\":\"材料延迟\",\"plan\":\"加快采购\"}"
            },
            "url": "http://localhost:8080/api/generate/report/work?type=周报"
          }
        }
      ]
    },
    {
      "name": "知识库",
      "item": [
        {
          "name": "初始化安全知识库",
          "request": {
            "method": "POST",
            "url": "http://localhost:8080/api/knowledge/init/safety"
          }
        },
        {
          "name": "搜索知识库",
          "request": {
            "method": "GET",
            "url": "http://localhost:8080/api/knowledge/search?keyword=安全"
          }
        }
      ]
    }
  ]
}
```

## 性能测试

使用 JMeter 或 wrk 进行压力测试：

```bash
# 使用 wrk 测试问答接口
wrk -t12 -c400 -d30s http://localhost:8080/api/chat/simple?question=测试
```

## 注意事项

1. 首次使用请先初始化知识库
2. 生产环境请调整限流配置
3. 向量数据库建议使用 Elasticsearch
4. 敏感信息请做好脱敏处理
