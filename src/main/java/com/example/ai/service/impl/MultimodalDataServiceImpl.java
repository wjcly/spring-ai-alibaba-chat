package com.example.ai.service.impl;

import com.example.ai.adapter.AiProviderAdapter;
import com.example.ai.adapter.AiProviderAdapterManager;
import com.example.ai.model.MultiModalRequest;
import com.example.ai.model.MultiModalResponse;
import com.example.ai.service.MultimodalDataService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多模态数据处理服务实现
 */
@Service
public class MultimodalDataServiceImpl implements MultimodalDataService {

    private static final Logger logger = LoggerFactory.getLogger(MultimodalDataServiceImpl.class);

    private final AiProviderAdapterManager adapterManager;

    public MultimodalDataServiceImpl(AiProviderAdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

    @Override
    public MultiModalResponse parseReport(InputStream fileStream, String fileName) {
        logger.info("解析报表文件：{}", fileName);

        String fileType = getFileType(fileName);

        // Java 8 兼容的 switch 语句
        if ("pdf".equals(fileType)) {
            return parsePdfReport(fileStream);
        } else if ("xls".equals(fileType) || "xlsx".equals(fileType)) {
            return parseExcelReport(fileStream, fileName);
        } else if ("jpg".equals(fileType) || "jpeg".equals(fileType) || 
                   "png".equals(fileType) || "gif".equals(fileType) || 
                   "bmp".equals(fileType)) {
            return parseImageReport(fileStream);
        } else {
            logger.warn("不支持的文件类型：{}", fileType);
            MultiModalResponse response = new MultiModalResponse();
            response.setContent("不支持的文件类型：" + fileType);
            return response;
        }
    }

    @Override
    public MultiModalResponse parsePdfReport(InputStream pdfStream) {
        logger.info("解析 PDF 报表");

        try {
            String textContent = extractPdfText(pdfStream);
            List<MultiModalResponse.ExtractedMetric> metrics = extractMetrics(textContent);
            MultiModalResponse.VisualizationSuggestion visualization = generateVisualization(metrics);

            AiProviderAdapter adapter = adapterManager.getDefaultAdapter();
            String analysisPrompt = buildAnalysisPrompt(textContent, metrics);
            String conclusion = adapter.chat(analysisPrompt);

            return MultiModalResponse.builder()
                    .content(conclusion)
                    .metrics(metrics)
                    .visualization(visualization)
                    .provider(adapter.getProvider().getCode())
                    .build();

        } catch (Exception e) {
            logger.error("PDF 报表解析失败：{}", e.getMessage(), e);
            MultiModalResponse response = new MultiModalResponse();
            response.setContent("PDF 解析失败：" + e.getMessage());
            return response;
        }
    }

    @Override
    public MultiModalResponse parseExcelReport(InputStream excelStream, String fileName) {
        logger.info("解析 Excel 报表：{}", fileName);

        try (Workbook workbook = new XSSFWorkbook(excelStream)) {
            StringBuilder content = new StringBuilder();
            List<MultiModalResponse.ExtractedMetric> metrics = new ArrayList<>();

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                content.append("=== ").append(sheet.getSheetName()).append(" ===\n\n");

                List<String[]> rows = new ArrayList<>();
                String[] headers = null;

                for (Row row : sheet) {
                    List<String> rowData = new ArrayList<>();
                    for (Cell cell : row) {
                        rowData.add(getCellValue(cell));
                    }
                    if (!rowData.isEmpty()) {
                        if (headers == null) {
                            headers = rowData.toArray(new String[0]);
                        } else {
                            rows.add(rowData.toArray(new String[0]));
                        }
                    }
                }

                content.append(formatTable(headers, rows)).append("\n\n");

                if (headers != null && headers.length >= 2) {
                    for (String[] row : rows) {
                        if (row.length >= 2) {
                            String value = parseNumericValue(row[row.length - 1]);
                            if (value != null) {
                                MultiModalResponse.ExtractedMetric metric = 
                                    new MultiModalResponse.ExtractedMetric();
                                metric.setName(row[0]);
                                metric.setValue(value);
                                metric.setPeriod(row.length > 2 ? row[1] : "");
                                metrics.add(metric);
                            }
                        }
                    }
                }
            }

            MultiModalResponse.VisualizationSuggestion visualization = generateVisualization(metrics);
            AiProviderAdapter adapter = adapterManager.getDefaultAdapter();
            String analysisPrompt = buildAnalysisPrompt(content.toString(), metrics);
            String conclusion = adapter.chat(analysisPrompt);

            return MultiModalResponse.builder()
                    .content(conclusion)
                    .metrics(metrics)
                    .visualization(visualization)
                    .provider(adapter.getProvider().getCode())
                    .build();

        } catch (Exception e) {
            logger.error("Excel 报表解析失败：{}", e.getMessage(), e);
            MultiModalResponse response = new MultiModalResponse();
            response.setContent("Excel 解析失败：" + e.getMessage());
            return response;
        }
    }

    @Override
    public MultiModalResponse parseImageReport(InputStream imageStream) {
        logger.info("解析图片报表");

        try {
            AiProviderAdapter adapter = adapterManager.getAdapter("alibaba");

            if (adapter.supportsMultimodal()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(imageStream);
                javax.imageio.ImageIO.write(image, "png", baos);
                String imageBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

                MultiModalRequest request = MultiModalRequest.builder()
                        .prompt("这是一张报表图片，请分析图片内容，提取关键数据和指标，并给出分析结论")
                        .imageBase64(imageBase64)
                        .build();

                MultiModalResponse response = adapter.multimodalChat(request);
                List<MultiModalResponse.ExtractedMetric> metrics = extractMetrics(response.getContent());
                MultiModalResponse.VisualizationSuggestion visualization = generateVisualization(metrics);

                response.setMetrics(metrics);
                response.setVisualization(visualization);

                return response;
            } else {
                String conclusion = adapter.chat("请分析这张报表图片的内容");
                List<MultiModalResponse.ExtractedMetric> metrics = extractMetrics(conclusion);

                return MultiModalResponse.builder()
                        .content(conclusion)
                        .metrics(metrics)
                        .visualization(generateVisualization(metrics))
                        .provider(adapter.getProvider().getCode())
                        .build();
            }

        } catch (Exception e) {
            logger.error("图片报表解析失败：{}", e.getMessage(), e);
            MultiModalResponse response = new MultiModalResponse();
            response.setContent("图片解析失败：" + e.getMessage());
            return response;
        }
    }

    @Override
    public List<MultiModalResponse.ExtractedMetric> extractMetrics(String content) {
        logger.debug("提取关键指标");

        List<MultiModalResponse.ExtractedMetric> metrics = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "([\\u4e00-\\u9fa5A-Za-z]+)[:：]?" +
                        "\\s*" +
                        "(-?\\d+(?:\\.\\d+)?)\\s*" +
                        "([%‰万元亿元吨公里个]?)",
                Pattern.MULTILINE);

        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String name = matcher.group(1).trim();
            String value = matcher.group(2);
            String unit = matcher.group(3);

            if (name.length() > 15 || name.matches("\\d+")) {
                continue;
            }

            MultiModalResponse.ExtractedMetric metric = new MultiModalResponse.ExtractedMetric();
            metric.setName(name);
            metric.setValue(value + unit);
            metric.setUnit(unit);
            metrics.add(metric);
        }

        return metrics;
    }

    @Override
    public MultiModalResponse.VisualizationSuggestion generateVisualization(
            List<MultiModalResponse.ExtractedMetric> metrics) {

        if (metrics == null || metrics.isEmpty()) {
            return null;
        }

        String chartType = "bar";
        String title = "数据可视化";
        List<String> dataPoints = new ArrayList<>();

        if (metrics.size() <= 5) {
            chartType = "pie";
        } else {
            for (MultiModalResponse.ExtractedMetric m : metrics) {
                if (m.getPeriod() != null && !m.getPeriod().isEmpty()) {
                    chartType = "line";
                    break;
                }
            }
        }

        for (MultiModalResponse.ExtractedMetric metric : metrics) {
            dataPoints.add(metric.getName() + ": " + metric.getValue());
        }

        return MultiModalResponse.VisualizationSuggestion.builder()
                .chartType(chartType)
                .title(title)
                .description("建议使用" + getChartTypeName(chartType) + "展示数据")
                .dataPoints(dataPoints)
                .build();
    }

    @Override
    public String generateConclusion(List<MultiModalResponse.ExtractedMetric> metrics, String context) {
        if (metrics == null || metrics.isEmpty()) {
            return "未提取到有效指标数据";
        }

        StringBuilder conclusion = new StringBuilder();
        conclusion.append("【数据概览】\n");
        conclusion.append("共提取到 ").append(metrics.size()).append(" 个指标。\n\n");

        conclusion.append("【关键指标】\n");
        for (int i = 0; i < Math.min(5, metrics.size()); i++) {
            MultiModalResponse.ExtractedMetric metric = metrics.get(i);
            conclusion.append("- ").append(metric.getName())
                    .append(": ").append(metric.getValue()).append("\n");
        }

        if (context != null && !context.isEmpty()) {
            conclusion.append("\n【分析】\n");
            conclusion.append(context);
        }

        return conclusion.toString();
    }

    @Override
    public String ocrImage(InputStream imageStream) {
        logger.info("OCR 识别图片");
        return "OCR 功能需要配置 Tesseract，当前返回示例结果。请配置 Tesseract-OCR 并使用 tess4j 库进行识别。";
    }

    @Override
    public String analyzeChart(InputStream imageStream) {
        logger.info("分析图表");

        AiProviderAdapter adapter = adapterManager.getAdapter("alibaba");

        if (adapter.supportsMultimodal()) {
            return adapter.analyzeImage(imageStream,
                    "请分析这张图表，提取图表类型、数据系列、数值范围、趋势等关键信息");
        } else {
            return "当前 AI 厂商不支持图表分析功能";
        }
    }

    // ==================== 辅助方法 ====================

    private String extractPdfText(InputStream pdfStream) throws IOException {
        try (PDDocument document = PDDocument.load(pdfStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }

    private String formatTable(String[] headers, List<String[]> rows) {
        StringBuilder sb = new StringBuilder();

        if (headers != null) {
            sb.append(String.join("\t", headers)).append("\n");
        }

        for (String[] row : rows) {
            sb.append(String.join("\t", row)).append("\n");
        }

        return sb.toString();
    }

    private String parseNumericValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        value = value.trim().replace(",", "");
        Pattern pattern = Pattern.compile("(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            return matcher.group(1) + value.substring(matcher.end()).trim();
        }
        return value;
    }

    private String getFileType(String fileName) {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }

    private String buildAnalysisPrompt(String content, List<MultiModalResponse.ExtractedMetric> metrics) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下报表数据，生成专业的分析结论：\n\n");

        prompt.append("【提取的指标】\n");
        for (MultiModalResponse.ExtractedMetric metric : metrics) {
            prompt.append("- ").append(metric.getName()).append(": ").append(metric.getValue()).append("\n");
        }

        prompt.append("\n【原始内容摘要】\n");
        prompt.append(content.substring(0, Math.min(1000, content.length())));
        prompt.append("...\n\n");

        prompt.append("请从以下角度分析：\n");
        prompt.append("1. 数据整体情况\n");
        prompt.append("2. 关键发现\n");
        prompt.append("3. 趋势分析\n");
        prompt.append("4. 建议措施\n");

        return prompt.toString();
    }

    private String getChartTypeName(String chartType) {
        if ("bar".equals(chartType)) return "柱状图";
        if ("line".equals(chartType)) return "折线图";
        if ("pie".equals(chartType)) return "饼图";
        if ("table".equals(chartType)) return "表格";
        return "图表";
    }
}
