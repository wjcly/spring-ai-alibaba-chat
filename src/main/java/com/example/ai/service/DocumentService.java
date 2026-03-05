package com.example.ai.service;

import com.example.ai.model.DocumentParseRequest;
import com.example.ai.model.DocumentParseResponse;

import java.io.InputStream;
import java.util.List;

/**
 * 文档解析服务接口
 * 支持多格式文档解析 (PDF/Word/Excel/TXT)
 */
public interface DocumentService {

    /**
     * 解析文档
     * @param request 解析请求
     * @return 解析结果
     */
    DocumentParseResponse parseDocument(DocumentParseRequest request);

    /**
     * 解析文档 (通过文件流)
     * @param inputStream 文件流
     * @param fileName 文件名
     * @param fileType 文件类型
     * @return 解析结果
     */
    DocumentParseResponse parseDocument(InputStream inputStream, String fileName, String fileType);

    /**
     * 提取文档中的表格
     * @param inputStream 文件流
     * @param fileName 文件名
     * @return 表格数据列表
     */
    List<List<List<String>>> extractTables(InputStream inputStream, String fileName);

    /**
     * 文档分块 (用于向量化)
     * @param content 文档内容
     * @param chunkSize 分块大小
     * @param overlap 重叠大小
     * @return 分块后的内容列表
     */
    List<String> chunkDocument(String content, int chunkSize, int overlap);

    /**
     * 智能解析文档并总结
     * @param inputStream 文件流
     * @param fileName 文件名
     * @return 文档总结
     */
    String summarizeDocument(InputStream inputStream, String fileName);
}
