package com.example.ai.service.impl;

import com.example.ai.adapter.AiProviderAdapterManager;
import com.example.ai.config.AiServiceProperties;
import com.example.ai.model.ChatResponse.ReferenceDocument;
import com.example.ai.model.KnowledgeDocument;
import com.example.ai.repository.KnowledgeDocumentRepository;
import com.example.ai.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * RAG 检索增强服务实现
 */
@Service
public class RagServiceImpl implements RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagServiceImpl.class);

    private final EmbeddingModel embeddingModel;
    private final KnowledgeDocumentRepository documentRepository;
    private final AiServiceProperties properties;
    private final AiProviderAdapterManager adapterManager;

    private final Map<String, float[]> vectorIndex = new ConcurrentHashMap<>();
    private final Map<String, KnowledgeDocument> documentIndex = new ConcurrentHashMap<>();

    public RagServiceImpl(EmbeddingModel embeddingModel,
                         KnowledgeDocumentRepository documentRepository,
                         AiServiceProperties properties,
                         AiProviderAdapterManager adapterManager) {
        this.embeddingModel = embeddingModel;
        this.documentRepository = documentRepository;
        this.properties = properties;
        this.adapterManager = adapterManager;
    }

    @Override
    public List<ReferenceDocument> retrieveRelevantDocuments(String question, String scene) {
        return retrieveRelevantDocuments(question, scene, null);
    }

    @Override
    public List<ReferenceDocument> retrieveRelevantDocuments(String question, String scene, String provider) {
        logger.debug("RAG 检索：question={}, scene={}, provider={}", question, scene, provider);

        try {
            float[] questionVector = embedDocument(question, provider);

            if (documentIndex.isEmpty()) {
                loadDocumentsToIndex(scene, provider);
            }

            List<ScoredDocument> scoredDocs = new ArrayList<>();
            for (Map.Entry<String, KnowledgeDocument> entry : documentIndex.entrySet()) {
                KnowledgeDocument doc = entry.getValue();

                if (scene != null && !scene.equals(doc.getScene()) && !"general".equals(doc.getScene())) {
                    continue;
                }

                float[] docVector = vectorIndex.get(entry.getKey());
                if (docVector != null) {
                    double similarity = cosineSimilarity(questionVector, docVector);
                    if (similarity >= properties.getRag().getSimilarityThreshold()) {
                        scoredDocs.add(new ScoredDocument(doc, similarity));
                    }
                }
            }

            return scoredDocs.stream()
                    .sorted(Comparator.comparingDouble(ScoredDocument::getSimilarity).reversed())
                    .limit(properties.getRag().getMaxDocuments())
                    .map(sd -> ReferenceDocument.builder()
                            .content(sd.getDocument().getContent())
                            .similarity(sd.getSimilarity())
                            .source(sd.getDocument().getSourcePath())
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("RAG 检索失败：{}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public void addDocument(String content, String title, String docType, String scene, List<String> keywords) {
        KnowledgeDocument document = KnowledgeDocument.builder()
                .title(title)
                .content(content)
                .docType(docType)
                .scene(scene)
                .keywords(keywords != null ? String.join(",", keywords) : null)
                .enabled(true)
                .build();

        KnowledgeDocument saved = documentRepository.save(document);

        float[] vector = embedDocument(content);
        String docId = "doc_" + saved.getId();
        vectorIndex.put(docId, vector);
        documentIndex.put(docId, saved);

        logger.info("文档已添加到知识库：title={}", title);
    }

    @Override
    public float[] embedDocument(String content) {
        return embedDocument(content, null);
    }

    /**
     * 向量化文档内容（支持指定 provider）
     */
    public float[] embedDocument(String content, String provider) {
        // 如果指定了 provider，使用对应的 EmbeddingModel
        if (provider != null && !"alibaba".equalsIgnoreCase(provider) && !"dashscope".equalsIgnoreCase(provider)) {
            // 对于其他 provider，目前统一使用 dashscope 的 EmbeddingModel
            // 后续可以根据需要扩展其他厂商的 EmbeddingModel
            logger.debug("使用默认 EmbeddingModel (provider={})", provider);
        }
        return embeddingModel.embed(content);
    }

    @Override
    public double cosineSimilarity(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("向量长度不一致");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    @Override
    @Transactional
    public void bulkImportDocuments(List<DocumentImportRequest> documents) {
        logger.info("批量导入文档：{} 个", documents.size());
        for (DocumentImportRequest req : documents) {
            addDocument(req.content(), req.title(), req.docType(), req.scene(), req.keywords());
        }
        logger.info("批量导入完成");
    }

    private void loadDocumentsToIndex(String scene) {
        loadDocumentsToIndex(scene, null);
    }

    private void loadDocumentsToIndex(String scene, String provider) {
        logger.info("加载文档到索引：scene={}, provider={}", scene, provider);

        List<KnowledgeDocument> documents;
        if (scene != null && !"general".equals(scene)) {
            documents = documentRepository.findBySceneAndEnabled(scene, true);
        } else {
            documents = documentRepository.findAll();
        }

        for (KnowledgeDocument doc : documents) {
            String docId = "doc_" + doc.getId();
            try {
                float[] vector = embedDocument(doc.getContent(), provider);
                vectorIndex.put(docId, vector);
                documentIndex.put(docId, doc);
            } catch (Exception e) {
                logger.warn("文档向量化失败：docId={}, error={}", docId, e.getMessage());
            }
        }

        logger.info("加载完成：{} 个文档", documentIndex.size());
    }

    private static class ScoredDocument {
        private final KnowledgeDocument document;
        private final double similarity;

        public ScoredDocument(KnowledgeDocument document, double similarity) {
            this.document = document;
            this.similarity = similarity;
        }

        public KnowledgeDocument getDocument() {
            return document;
        }

        public double getSimilarity() {
            return similarity;
        }
    }
}
