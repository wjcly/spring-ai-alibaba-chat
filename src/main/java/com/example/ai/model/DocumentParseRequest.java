package com.example.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档解析请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentParseRequest {

    /**
     * 文件路径或 URL
     */
    private String filePath;

    /**
     * 文件内容 (Base64 或直接文本)
     */
    private String fileContent;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件类型 (pdf/doc/docx/xls/xlsx/txt)
     */
    private String fileType;

    /**
     * 是否提取表格
     */
    @Builder.Default
    private Boolean extractTables = true;

    /**
     * 是否提取图片
     */
    @Builder.Default
    private Boolean extractImages = false;
}
