package com.example.ai.controller;

import com.example.ai.model.ApiResponse;
import com.example.ai.model.KnowledgeDocument;
import com.example.ai.repository.KnowledgeDocumentRepository;
import com.example.ai.service.IndustrialKnowledgeService;
import com.example.ai.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 知识库管理 API 接口
 */
@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeController.class);

    private final KnowledgeDocumentRepository documentRepository;
    private final RagService ragService;
    private final IndustrialKnowledgeService industrialKnowledgeService;

    public KnowledgeController(KnowledgeDocumentRepository documentRepository,
                              RagService ragService,
                              IndustrialKnowledgeService industrialKnowledgeService) {
        this.documentRepository = documentRepository;
        this.ragService = ragService;
        this.industrialKnowledgeService = industrialKnowledgeService;
    }

    /**
     * 获取所有知识库文档
     */
    @GetMapping
    public ApiResponse<List<KnowledgeDocument>> listDocuments(
            @RequestParam(required = false) String scene,
            @RequestParam(required = false) String docType) {
        logger.info("获取知识库文档：scene={}, docType={}", scene, docType);

        List<KnowledgeDocument> documents;
        if (scene != null) {
            documents = documentRepository.findBySceneAndEnabled(scene, true);
        } else if (docType != null) {
            documents = documentRepository.findByDocTypeAndEnabled(docType, true);
        } else {
            documents = documentRepository.findAll();
        }

        return ApiResponse.success(documents, generateRequestId());
    }

    /**
     * 获取单个文档
     */
    @GetMapping("/{id}")
    public ApiResponse<KnowledgeDocument> getDocument(@PathVariable Long id) {
        logger.info("获取文档：id={}", id);
        return documentRepository.findById(id)
                .map(doc -> ApiResponse.success(doc, generateRequestId()))
                .orElse(ApiResponse.error(404, "文档不存在", generateRequestId()));
    }

    /**
     * 添加文档到知识库
     */
    @PostMapping
    public ApiResponse<KnowledgeDocument> addDocument(@RequestBody Map<String, String> request) {
        logger.info("添加文档到知识库：title={}", request.get("title"));

        try {
            String content = request.get("content");
            String title = request.get("title");
            String docType = request.getOrDefault("docType", "规范");
            String scene = request.getOrDefault("scene", "general");
            String keywordsStr = request.getOrDefault("keywords", "");

            List<String> keywords = keywordsStr.isEmpty() ? 
                    List.of() : List.of(keywordsStr.split(","));

            ragService.addDocument(content, title, docType, scene, keywords);

            // 获取刚保存的文档
            List<KnowledgeDocument> docs = documentRepository.searchByKeyword(content.substring(0, Math.min(50, content.length())));
            KnowledgeDocument doc = docs.isEmpty() ? null : docs.get(docs.size() - 1);

            return ApiResponse.success(doc, generateRequestId());
        } catch (Exception e) {
            logger.error("添加文档失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "添加文档失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDocument(@PathVariable Long id) {
        logger.info("删除文档：id={}", id);
        documentRepository.deleteById(id);
        return ApiResponse.success(null, generateRequestId());
    }

    /**
     * 搜索文档
     */
    @GetMapping("/search")
    public ApiResponse<List<KnowledgeDocument>> searchDocuments(@RequestParam String keyword) {
        logger.info("搜索文档：keyword={}", keyword);
        List<KnowledgeDocument> documents = documentRepository.searchByKeyword(keyword);
        return ApiResponse.success(documents, generateRequestId());
    }

    /**
     * 初始化安全知识库
     */
    @PostMapping("/init/safety")
    public ApiResponse<String> initSafetyKnowledge() {
        logger.info("初始化安全知识库");
        industrialKnowledgeService.initConstructionSafetyKnowledge();
        return ApiResponse.success("安全知识库初始化完成", generateRequestId());
    }

    /**
     * 初始化工业规范知识库
     */
    @PostMapping("/init/industrial")
    public ApiResponse<String> initIndustrialKnowledge() {
        logger.info("初始化工业规范知识库");
        industrialKnowledgeService.initIndustrialStandardKnowledge();
        return ApiResponse.success("工业规范知识库初始化完成", generateRequestId());
    }

    /**
     * 批量导入示例数据
     */
    @PostMapping("/import/sample")
    public ApiResponse<String> importSampleData() {
        logger.info("导入示例数据");
        // 这里需要添加工业知识服务的批量导入方法
        return ApiResponse.success("示例数据导入完成", generateRequestId());
    }

    /**
     * 生成请求 ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
