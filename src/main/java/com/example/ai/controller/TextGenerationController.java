package com.example.ai.controller;

import com.example.ai.model.ApiResponse;
import com.example.ai.service.TextGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文本生成 API 接口
 */
@RestController
@RequestMapping("/generate")
public class TextGenerationController {

    private static final Logger logger = LoggerFactory.getLogger(TextGenerationController.class);

    private final TextGenerationService textGenerationService;

    public TextGenerationController(TextGenerationService textGenerationService) {
        this.textGenerationService = textGenerationService;
    }

    /**
     * 生成工作报告
     */
    @PostMapping("/report/work")
    public ApiResponse<String> generateWorkReport(
            @RequestBody Map<String, Object> workData,
            @RequestParam(defaultValue = "周报") String type) {
        logger.info("生成工作报告：type={}", type);
        try {
            String report = textGenerationService.generateWorkReport(workData, type);
            return ApiResponse.success(report, generateRequestId());
        } catch (Exception e) {
            logger.error("报告生成失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "报告生成失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 生成安全检查报告
     */
    @PostMapping("/report/safety")
    public ApiResponse<String> generateSafetyReport(
            @RequestBody Map<String, Object> inspectionData,
            @RequestParam(defaultValue = "工地") String scene) {
        logger.info("生成安全检查报告：scene={}", scene);
        try {
            String report = textGenerationService.generateSafetyReport(inspectionData, scene);
            return ApiResponse.success(report, generateRequestId());
        } catch (Exception e) {
            logger.error("报告生成失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "报告生成失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 生成会议纪要
     */
    @PostMapping("/meeting-minutes")
    public ApiResponse<String> generateMeetingMinutes(
            @RequestParam String notes,
            @RequestParam(required = false) List<String> participants) {
        logger.info("生成会议纪要");
        try {
            String minutes = textGenerationService.generateMeetingMinutes(notes, participants);
            return ApiResponse.success(minutes, generateRequestId());
        } catch (Exception e) {
            logger.error("纪要生成失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "纪要生成失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 生成技术文档
     */
    @PostMapping("/technical-doc")
    public ApiResponse<String> generateTechnicalDocument(
            @RequestParam String topic,
            @RequestBody Map<String, Object> requestData) {
        logger.info("生成技术文档：topic={}", topic);
        try {
            @SuppressWarnings("unchecked")
            List<String> outline = (List<String>) requestData.get("outline");
            @SuppressWarnings("unchecked")
            Map<String, String> requirements = (Map<String, String>) requestData.get("requirements");
            
            String document = textGenerationService.generateTechnicalDocument(topic, outline, requirements);
            return ApiResponse.success(document, generateRequestId());
        } catch (Exception e) {
            logger.error("文档生成失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "文档生成失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 内容润色
     */
    @PostMapping("/polish")
    public ApiResponse<String> polishContent(
            @RequestBody String content,
            @RequestParam(defaultValue = "正式") String style) {
        logger.info("润色内容：style={}", style);
        try {
            String polished = textGenerationService.polishContent(content, style);
            return ApiResponse.success(polished, generateRequestId());
        } catch (Exception e) {
            logger.error("润色失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "润色失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 内容扩写
     */
    @PostMapping("/expand")
    public ApiResponse<String> expandContent(
            @RequestBody String brief,
            @RequestParam(defaultValue = "500") int targetLength) {
        logger.info("扩写内容：targetLength={}", targetLength);
        try {
            String expanded = textGenerationService.expandContent(brief, targetLength);
            return ApiResponse.success(expanded, generateRequestId());
        } catch (Exception e) {
            logger.error("扩写失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "扩写失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 生成通知/公告
     */
    @PostMapping("/notice")
    public ApiResponse<String> generateNotice(
            @RequestParam String topic,
            @RequestBody List<String> keyPoints,
            @RequestParam(defaultValue = "全体员工") String audience) {
        logger.info("生成通知：topic={}", topic);
        try {
            String notice = textGenerationService.generateNotice(topic, keyPoints, audience);
            return ApiResponse.success(notice, generateRequestId());
        } catch (Exception e) {
            logger.error("通知生成失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "通知生成失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 生成培训材料
     */
    @PostMapping("/training")
    public ApiResponse<String> generateTrainingMaterial(
            @RequestParam String topic,
            @RequestParam(defaultValue = "初级") String level,
            @RequestParam(defaultValue = "2 小时") String duration) {
        logger.info("生成培训材料：topic={}, level={}", topic, level);
        try {
            String material = textGenerationService.generateTrainingMaterial(topic, level, duration);
            return ApiResponse.success(material, generateRequestId());
        } catch (Exception e) {
            logger.error("培训材料生成失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "培训材料生成失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 生成请求 ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
