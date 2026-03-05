package com.example.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 智能问答请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * 用户问题
     */
    @NotBlank(message = "问题不能为空")
    @Size(max = 2000, message = "问题长度不能超过 2000 字符")
    private String question;

    /**
     * 会话 ID (用于多轮对话)
     */
    private String conversationId;

    /**
     * 是否启用 RAG 检索增强
     */
    private Boolean useRag = true;

    /**
     * 系统提示词 (可选)
     */
    private String systemPrompt;

    /**
     * 温度参数 (0-1, 越高越随机)
     */
    private Double temperature = 0.7;

    /**
     * 场景类型 (工地安全/工业规范/通用)
     */
    private String scene = "general";

    /**
     * AI 厂商代码 (alibaba/deepseek/tencent/openai 等)
     */
    private String provider;
}
