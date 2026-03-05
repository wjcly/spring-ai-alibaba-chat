package com.example.ai.service;

import com.example.ai.model.MultiModalResponse;

import java.io.InputStream;
import java.util.List;

/**
 * 多模态数据处理服务
 * 支持 PDF/Excel/图片格式的报表解析，自动提取关键指标并生成可视化结论
 */
public interface MultimodalDataService {

    /**
     * 解析报表文件 (自动识别格式)
     * @param fileStream 文件流
     * @param fileName 文件名
     * @return 解析结果
     */
    MultiModalResponse parseReport(InputStream fileStream, String fileName);

    /**
     * 解析 PDF 报表
     * @param pdfStream PDF 流
     * @return 解析结果
     */
    MultiModalResponse parsePdfReport(InputStream pdfStream);

    /**
     * 解析 Excel 报表
     * @param excelStream Excel 流
     * @param fileName 文件名
     * @return 解析结果
     */
    MultiModalResponse parseExcelReport(InputStream excelStream, String fileName);

    /**
     * 解析图片报表 (OCR + 图表识别)
     * @param imageStream 图片流
     * @return 解析结果
     */
    MultiModalResponse parseImageReport(InputStream imageStream);

    /**
     * 提取关键指标
     * @param content 报表内容
     * @return 指标列表
     */
    List<MultiModalResponse.ExtractedMetric> extractMetrics(String content);

    /**
     * 生成可视化建议
     * @param metrics 指标数据
     * @return 可视化建议
     */
    MultiModalResponse.VisualizationSuggestion generateVisualization(List<MultiModalResponse.ExtractedMetric> metrics);

    /**
     * 生成分析结论
     * @param metrics 指标数据
     * @param context 上下文
     * @return 分析结论
     */
    String generateConclusion(List<MultiModalResponse.ExtractedMetric> metrics, String context);

    /**
     * OCR 识别图片文字
     * @param imageStream 图片流
     * @return 识别的文本
     */
    String ocrImage(InputStream imageStream);

    /**
     * 分析图表 (柱状图/折线图/饼图等)
     * @param imageStream 图表图片流
     * @return 图表数据分析结果
     */
    String analyzeChart(InputStream imageStream);
}
