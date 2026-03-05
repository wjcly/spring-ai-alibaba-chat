package com.example.ai.service.impl;

import com.example.ai.config.AiServiceProperties;
import com.example.ai.model.DocumentParseRequest;
import com.example.ai.model.DocumentParseResponse;
import com.example.ai.model.DocumentParseResponse.DocumentChunk;
import com.example.ai.model.DocumentParseResponse.DocumentMetadata;
import com.example.ai.service.DocumentService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文档解析服务实现
 * 支持 PDF、Word、Excel、TXT 等格式解析
 */
@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);

    private final ChatClient chatClient;
    private final AiServiceProperties properties;

    public DocumentServiceImpl(ChatClient chatClient, AiServiceProperties properties) {
        this.chatClient = chatClient;
        this.properties = properties;
    }

    @Override
    public DocumentParseResponse parseDocument(DocumentParseRequest request) {
        logger.info("解析文档：fileName={}, fileType={}", request.getFileName(), request.getFileType());

        try {
            if (request.getFileContent() != null) {
                // 从 Base64 或文本内容解析
                byte[] content = Base64.getDecoder().decode(request.getFileContent());
                try (InputStream is = new ByteArrayInputStream(content)) {
                    return parseDocument(is, request.getFileName(), request.getFileType());
                }
            } else if (request.getFilePath() != null) {
                // 从文件路径解析
                try (InputStream is = new FileInputStream(request.getFilePath())) {
                    return parseDocument(is, request.getFileName(), request.getFileType());
                }
            } else {
                throw new IllegalArgumentException("文件内容或路径不能为空");
            }
        } catch (Exception e) {
            logger.error("文档解析失败：{}", e.getMessage(), e);
            return DocumentParseResponse.builder()
                    .textContent("解析失败：" + e.getMessage())
                    .build();
        }
    }

    @Override
    public DocumentParseResponse parseDocument(InputStream inputStream, String fileName, String fileType) {
        String content = "";
        DocumentMetadata metadata = DocumentMetadata.builder()
                .fileName(fileName)
                .fileType(fileType)
                .build();
        List<List<List<String>>> tables = new ArrayList<>();

        try {
            switch (fileType.toLowerCase()) {
                case "pdf":
                    content = parsePdf(inputStream);
                    break;
                case "doc":
                case "docx":
                    content = parseWord(inputStream);
                    break;
                case "xls":
                case "xlsx":
                    content = parseExcel(inputStream, tables);
                    break;
                case "txt":
                    content = parseText(inputStream);
                    break;
                default:
                    content = parseText(inputStream);
            }

            // 构建分块
            List<DocumentChunk> chunks = chunkDocumentToObjects(content, 
                    properties.getRag().getChunkSize(),
                    properties.getRag().getChunkOverlap());

            return DocumentParseResponse.builder()
                    .textContent(content)
                    .tables(tables.stream()
                            .map(table -> {
                                Map<String, Object> tableMap = new HashMap<>();
                                tableMap.put("headers", table.get(0));
                                tableMap.put("rows", table.subList(1, table.size()));
                                return tableMap;
                            })
                            .collect(Collectors.toList()))
                    .metadata(metadata)
                    .chunks(chunks)
                    .build();

        } catch (Exception e) {
            logger.error("文档解析失败：{}", e.getMessage(), e);
            throw new RuntimeException("文档解析失败", e);
        }
    }

    @Override
    public List<List<List<String>>> extractTables(InputStream inputStream, String fileName) {
        List<List<List<String>>> tables = new ArrayList<>();
        String fileType = getFileTypeFromFileName(fileName);

        try {
            if ("xlsx".equals(fileType) || "xls".equals(fileType)) {
                parseExcel(inputStream, tables);
            }
        } catch (Exception e) {
            logger.error("表格提取失败：{}", e.getMessage(), e);
        }

        return tables;
    }

    @Override
    public List<String> chunkDocument(String content, int chunkSize, int overlap) {
        return chunkDocumentToObjects(content, chunkSize, overlap)
                .stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.toList());
    }

    @Override
    public String summarizeDocument(InputStream inputStream, String fileName) {
        // 先解析文档
        String fileType = getFileTypeFromFileName(fileName);
        String content;

        try {
            switch (fileType.toLowerCase()) {
                case "pdf":
                    content = parsePdf(inputStream);
                    break;
                case "doc":
                case "docx":
                    content = parseWord(inputStream);
                    break;
                case "txt":
                    content = parseText(inputStream);
                    break;
                default:
                    content = parseText(inputStream);
            }

            // 使用 AI 总结
            String prompt = "请总结以下文档内容，提取关键信息，生成简洁的摘要 (300 字以内)：\n\n" + content;
            
            return chatClient.prompt(prompt)
                    .call()
                    .content();

        } catch (Exception e) {
            logger.error("文档总结失败：{}", e.getMessage(), e);
            return "文档总结失败：" + e.getMessage();
        }
    }

    /**
     * 解析 PDF
     */
    private String parsePdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 解析 Word
     */
    private String parseWord(InputStream inputStream) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText()).append("\n");
            }
        }
        
        return content.toString();
    }

    /**
     * 解析 Excel
     */
    private String parseExcel(InputStream inputStream, List<List<List<String>>> tables) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            int numberOfSheets = workbook.getNumberOfSheets();
            
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                content.append("=== Sheet: ").append(sheet.getSheetName()).append(" ===\n\n");
                
                List<List<String>> table = new ArrayList<>();
                
                for (Row row : sheet) {
                    List<String> rowData = new ArrayList<>();
                    StringBuilder rowText = new StringBuilder();
                    
                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        rowData.add(cellValue);
                        rowText.append(cellValue).append("\t");
                    }
                    
                    table.add(rowData);
                    content.append(rowText).append("\n");
                }
                
                if (!table.isEmpty()) {
                    tables.add(table);
                }
                
                content.append("\n");
            }
        }
        
        return content.toString();
    }

    /**
     * 解析 TXT
     */
    private String parseText(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 获取单元格值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 文档分块
     */
    private List<DocumentChunk> chunkDocumentToObjects(String content, int chunkSize, int overlap) {
        List<DocumentChunk> chunks = new ArrayList<>();
        
        if (content == null || content.isEmpty()) {
            return chunks;
        }

        // 按段落分割
        String[] paragraphs = content.split("\n\n+");
        StringBuilder currentChunk = new StringBuilder();
        int chunkIndex = 0;

        for (String paragraph : paragraphs) {
            if (currentChunk.length() + paragraph.length() <= chunkSize) {
                currentChunk.append(paragraph).append("\n\n");
            } else {
                if (currentChunk.length() > 0) {
                    chunks.add(DocumentChunk.builder()
                            .content(currentChunk.toString().trim())
                            .chunkIndex(chunkIndex++)
                            .build());
                }
                currentChunk = new StringBuilder(paragraph).append("\n\n");
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(DocumentChunk.builder()
                    .content(currentChunk.toString().trim())
                    .chunkIndex(chunkIndex++)
                    .build());
        }

        // 设置总数
        int total = chunks.size();
        for (DocumentChunk chunk : chunks) {
            chunk.setTotalChunks(total);
        }

        return chunks;
    }

    /**
     * 从文件名获取文件类型
     */
    private String getFileTypeFromFileName(String fileName) {
        if (fileName == null) {
            return "txt";
        }
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1);
        }
        return "txt";
    }
}
