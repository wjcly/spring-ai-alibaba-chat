package com.example.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 多模态响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiModalResponse {

    /**
     * AI 响应内容
     */
    private String content;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * AI 厂商
     */
    private String provider;

    /**
     * Token 使用情况
     */
    private TokenUsage usage;

    /**
     * 提取的指标数据 (用于报表分析)
     */
    private List<ExtractedMetric> metrics;

    /**
     * 可视化建议
     */
    private VisualizationSuggestion visualization;

    /**
     * 原始响应 (用于调试)
     */
    private String rawResponse;

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

    /**
     * 提取的指标
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedMetric {
        private String name;
        private String value;
        private String unit;
        private String period;
        private Double changeRate;
    }

    /**
     * 可视化建议
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisualizationSuggestion {
        private String chartType; // bar/line/pie/table
        private String title;
        private String description;
        private List<String> dataPoints;
    }
}
