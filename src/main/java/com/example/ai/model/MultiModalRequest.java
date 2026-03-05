package com.example.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.util.List;

/**
 * 多模态请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiModalRequest {

    /**
     * 文本提示词
     */
    private String prompt;

    /**
     * 图片流 (可选)
     */
    private InputStream imageStream;

    /**
     * 图片 Base64 (可选)
     */
    private String imageBase64;

    /**
     * 图片 URL (可选)
     */
    private String imageUrl;

    /**
     * 文档流 (可选)
     */
    private InputStream documentStream;

    /**
     * 文档 Base64 (可选)
     */
    private String documentBase64;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 温度参数
     */
    @Builder.Default
    private Double temperature = 0.7;

    /**
     * 最大输出长度
     */
    @Builder.Default
    private Integer maxTokens = 2048;

    /**
     * 对话历史
     */
    private List<ChatMessage> messages;

    /**
     * 聊天消息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role; // user/assistant/system
        private String content;
        private String imageUrl; // 多模态消息
    }
}
