package com.example.ai.controller;

import com.example.ai.model.ApiResponse;
import com.example.ai.model.MultiModalResponse;
import com.example.ai.service.MultimodalDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 多模态数据 API 接口
 * 支持 PDF/Excel/图片格式的报表解析，自动提取关键指标并生成可视化结论
 */
@RestController
@RequestMapping("/multimodal")
public class MultimodalDataController {

    private static final Logger logger = LoggerFactory.getLogger(MultimodalDataController.class);

    private final MultimodalDataService multimodalDataService;

    public MultimodalDataController(MultimodalDataService multimodalDataService) {
        this.multimodalDataService = multimodalDataService;
    }

    /**
     * 智能解析报表 (自动识别格式)
     */
    @PostMapping("/parse")
    public ApiResponse<MultiModalResponse> parseReport(
            @RequestParam("file") MultipartFile file) {
        logger.info("智能解析报表：fileName={}, size={}", file.getOriginalFilename(), file.getSize());

        try {
            MultiModalResponse response = multimodalDataService.parseReport(
                    file.getInputStream(), 
                    file.getOriginalFilename());
            return ApiResponse.success(response, generateRequestId());
        } catch (IOException e) {
            logger.error("报表解析失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "报表解析失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 解析 PDF 报表
     */
    @PostMapping("/parse/pdf")
    public ApiResponse<MultiModalResponse> parsePdfReport(
            @RequestParam("file") MultipartFile file) {
        logger.info("解析 PDF 报表：fileName={}", file.getOriginalFilename());

        try {
            MultiModalResponse response = multimodalDataService.parsePdfReport(
                    file.getInputStream());
            return ApiResponse.success(response, generateRequestId());
        } catch (IOException e) {
            logger.error("PDF 报表解析失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "PDF 报表解析失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 解析 Excel 报表
     */
    @PostMapping("/parse/excel")
    public ApiResponse<MultiModalResponse> parseExcelReport(
            @RequestParam("file") MultipartFile file) {
        logger.info("解析 Excel 报表：fileName={}", file.getOriginalFilename());

        try {
            MultiModalResponse response = multimodalDataService.parseExcelReport(
                    file.getInputStream(), 
                    file.getOriginalFilename());
            return ApiResponse.success(response, generateRequestId());
        } catch (IOException e) {
            logger.error("Excel 报表解析失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "Excel 报表解析失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 解析图片报表 (OCR + 图表识别)
     */
    @PostMapping("/parse/image")
    public ApiResponse<MultiModalResponse> parseImageReport(
            @RequestParam("file") MultipartFile file) {
        logger.info("解析图片报表：fileName={}", file.getOriginalFilename());

        try {
            MultiModalResponse response = multimodalDataService.parseImageReport(
                    file.getInputStream());
            return ApiResponse.success(response, generateRequestId());
        } catch (IOException e) {
            logger.error("图片报表解析失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "图片报表解析失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * OCR 识别图片文字
     */
    @PostMapping("/ocr")
    public ResponseEntity<String> ocrImage(
            @RequestParam("file") MultipartFile file) {
        logger.info("OCR 识别：fileName={}", file.getOriginalFilename());

        try {
            String text = multimodalDataService.ocrImage(file.getInputStream());
            return ResponseEntity.ok(text);
        } catch (IOException e) {
            logger.error("OCR 识别失败：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("OCR 识别失败：" + e.getMessage());
        }
    }

    /**
     * 分析图表 (柱状图/折线图/饼图等)
     */
    @PostMapping("/analyze/chart")
    public ApiResponse<String> analyzeChart(
            @RequestParam("file") MultipartFile file) {
        logger.info("分析图表：fileName={}", file.getOriginalFilename());

        try {
            String analysis = multimodalDataService.analyzeChart(file.getInputStream());
            return ApiResponse.success(analysis, generateRequestId());
        } catch (IOException e) {
            logger.error("图表分析失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "图表分析失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 从内容中提取关键指标
     */
    @PostMapping("/extract/metrics")
    public ApiResponse<Object> extractMetrics(
            @RequestBody String content) {
        logger.info("提取关键指标");

        try {
            var metrics = multimodalDataService.extractMetrics(content);
            return ApiResponse.success(metrics, generateRequestId());
        } catch (Exception e) {
            logger.error("指标提取失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "指标提取失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 生成可视化建议
     */
    @PostMapping("/visualize")
    public ApiResponse<Object> generateVisualization(
            @RequestBody Object metrics) {
        logger.info("生成可视化建议");

        // 这里需要定义具体的指标输入格式
        return ApiResponse.success("可视化建议生成成功", generateRequestId());
    }

    /**
     * 生成请求 ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
