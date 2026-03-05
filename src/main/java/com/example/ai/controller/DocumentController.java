package com.example.ai.controller;

import com.example.ai.model.ApiResponse;
import com.example.ai.model.DocumentParseRequest;
import com.example.ai.model.DocumentParseResponse;
import com.example.ai.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 文档解析 API 接口
 */
@RestController
@RequestMapping("/document")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * 上传并解析文档
     */
    @PostMapping("/parse")
    public ApiResponse<DocumentParseResponse> parseDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "extractTables", defaultValue = "true") Boolean extractTables,
            @RequestParam(value = "extractImages", defaultValue = "false") Boolean extractImages) {
        logger.info("解析文档：fileName={}, size={}", file.getOriginalFilename(), file.getSize());

        try {
            String fileName = file.getOriginalFilename();
            String fileType = getFileExtension(fileName);

            byte[] fileContent = file.getBytes();
            String base64Content = java.util.Base64.getEncoder().encodeToString(fileContent);

            DocumentParseRequest request = DocumentParseRequest.builder()
                    .fileName(fileName)
                    .fileType(fileType)
                    .fileContent(base64Content)
                    .extractTables(extractTables)
                    .extractImages(extractImages)
                    .build();

            DocumentParseResponse response = documentService.parseDocument(request);
            return ApiResponse.success(response, generateRequestId());

        } catch (IOException e) {
            logger.error("文档解析失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "文档解析失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 解析文档（通过 JSON 请求）
     */
    @PostMapping("/parse/json")
    public ApiResponse<DocumentParseResponse> parseDocumentJson(
            @RequestBody DocumentParseRequest request) {
        logger.info("解析文档（JSON）: fileName={}", request.getFileName());

        try {
            DocumentParseResponse response = documentService.parseDocument(request);
            return ApiResponse.success(response, generateRequestId());
        } catch (Exception e) {
            logger.error("文档解析失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "文档解析失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 文档总结
     */
    @PostMapping("/summarize")
    public ResponseEntity<String> summarizeDocument(@RequestParam("file") MultipartFile file) {
        logger.info("总结文档：fileName={}", file.getOriginalFilename());

        try {
            String fileName = file.getOriginalFilename();
            String summary = documentService.summarizeDocument(file.getInputStream(), fileName);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("文档总结失败：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("文档总结失败：" + e.getMessage());
        }
    }

    /**
     * 提取表格
     */
    @PostMapping("/extract/tables")
    public ApiResponse<Object> extractTables(@RequestParam("file") MultipartFile file) {
        logger.info("提取表格：fileName={}", file.getOriginalFilename());

        try {
            String fileName = file.getOriginalFilename();
            List<List<List<String>>> tables = documentService.extractTables(file.getInputStream(), fileName);
            return ApiResponse.success(tables, generateRequestId());
        } catch (Exception e) {
            logger.error("表格提取失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "表格提取失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return "txt";
        }
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "txt";
    }

    /**
     * 生成请求 ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
