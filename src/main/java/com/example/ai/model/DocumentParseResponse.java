package com.example.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 文档解析响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentParseResponse {

    /**
     * 解析后的文本内容
     */
    private String textContent;

    /**
     * 提取的表格数据
     */
    private List<Map<String, Object>> tables;

    /**
     * 文档元数据
     */
    private DocumentMetadata metadata;

    /**
     * 分块后的内容 (用于向量化)
     */
    private List<DocumentChunk> chunks;

    /**
     * 文档元数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentMetadata {
        private String fileName;
        private String fileType;
        private Long fileSize;
        private Integer pageCount;
        private String author;
        private String createTime;
    }

    /**
     * 文档分块
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentChunk {
        private String content;
        private Integer chunkIndex;
        private Integer totalChunks;
    }
}
