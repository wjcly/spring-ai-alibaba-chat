package com.example.ai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RAG 知识库文档实体
 */
@Entity
@Table(name = "knowledge_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocument {

    /**
     * 主键 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文档标题
     */
    @Column(length = 500)
    private String title;

    /**
     * 文档内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 文档类型 (规范/流程/案例/FAQ)
     */
    @Column(length = 50)
    private String docType;

    /**
     * 所属场景 (工地安全/工业规范/通用)
     */
    @Column(length = 50)
    private String scene;

    /**
     * 关键词标签
     */
    @Column(length = 1000)
    private String keywords;

    /**
     * 向量嵌入 (生产环境应使用向量数据库)
     */
    @Column(columnDefinition = "TEXT")
    private String embedding;

    /**
     * 来源文件路径
     */
    @Column(length = 500)
    private String sourcePath;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否启用
     */
    @Builder.Default
    private Boolean enabled = true;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
