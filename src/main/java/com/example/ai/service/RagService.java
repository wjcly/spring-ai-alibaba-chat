package com.example.ai.service;

import com.example.ai.model.ChatResponse.ReferenceDocument;

import java.util.List;

/**
 * RAG 检索增强服务接口
 */
public interface RagService {

    /**
     * 检索与问题相关的文档
     * @param question 用户问题
     * @param scene 场景类型
     * @return 相关文档列表
     */
    List<ReferenceDocument> retrieveRelevantDocuments(String question, String scene);

    /**
     * 检索与问题相关的文档（指定 AI 厂商）
     * @param question 用户问题
     * @param scene 场景类型
     * @param provider AI 厂商代码
     * @return 相关文档列表
     */
    List<ReferenceDocument> retrieveRelevantDocuments(String question, String scene, String provider);

    /**
     * 添加文档到知识库
     * @param content 文档内容
     * @param title 文档标题
     * @param docType 文档类型
     * @param scene 所属场景
     * @param keywords 关键词
     */
    void addDocument(String content, String title, String docType, String scene, List<String> keywords);

    /**
     * 向量化文档内容
     * @param content 文档内容
     * @return 向量嵌入
     */
    float[] embedDocument(String content);

    /**
     * 计算相似度
     * @param vector1 向量 1
     * @param vector2 向量 2
     * @return 相似度分数 (0-1)
     */
    double cosineSimilarity(float[] vector1, float[] vector2);

    /**
     * 批量导入文档
     * @param documents 文档列表
     */
    void bulkImportDocuments(List<DocumentImportRequest> documents);

    /**
     * 文档导入请求 (Java 8 兼容)
     */
    class DocumentImportRequest {
        private final String content;
        private final String title;
        private final String docType;
        private final String scene;
        private final List<String> keywords;

        public DocumentImportRequest(String content, String title, String docType, 
                                     String scene, List<String> keywords) {
            this.content = content;
            this.title = title;
            this.docType = docType;
            this.scene = scene;
            this.keywords = keywords;
        }

        public String content() { return content; }
        public String title() { return title; }
        public String docType() { return docType; }
        public String scene() { return scene; }
        public List<String> keywords() { return keywords; }
    }
}
