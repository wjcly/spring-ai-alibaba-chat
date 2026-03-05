package com.example.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 智能问答响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * AI 回答内容
     */
    private String answer;

    /**
     * 会话 ID
     */
    private String conversationId;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * RAG 检索到的参考文档
     */
    private List<ReferenceDocument> references;

    /**
     * Token 使用情况
     */
    private TokenUsage tokenUsage;

    /**
     * 回答生成时间 (毫秒)
     */
    private Long durationMs;

    /**
     * 参考文档信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferenceDocument {
        private String content;
        private Double similarity;
        private String source;
    }

    /**
     * Token 使用统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
