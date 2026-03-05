package com.example.ai.repository;

import com.example.ai.model.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 知识库文档 Repository
 */
@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    /**
     * 按场景查询文档
     */
    List<KnowledgeDocument> findBySceneAndEnabled(String scene, Boolean enabled);

    /**
     * 按文档类型查询
     */
    List<KnowledgeDocument> findByDocTypeAndEnabled(String docType, Boolean enabled);

    /**
     * 搜索包含关键词的文档
     */
    @Query("SELECT k FROM KnowledgeDocument k WHERE k.content LIKE %:keyword% AND k.enabled = true")
    List<KnowledgeDocument> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 按场景和关键词搜索
     */
    @Query("SELECT k FROM KnowledgeDocument k WHERE k.scene = :scene AND " +
           "(k.content LIKE %:keyword% OR k.keywords LIKE %:keyword%) AND k.enabled = true")
    List<KnowledgeDocument> searchBySceneAndKeyword(
            @Param("scene") String scene,
            @Param("keyword") String keyword);
}
