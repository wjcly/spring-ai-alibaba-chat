package com.example.ai.controller;

import com.example.ai.model.ApiResponse;
import com.example.ai.service.AiChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 智能问答 API 接口
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final AiChatService aiChatService;

    public ChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * 简单问答
     */
    @PostMapping("/simple")
    public ApiResponse<Map<String, Object>> simpleChat(@RequestParam String question) {
        logger.info("简单问答：question={}", question);
        try {
            String answer = aiChatService.chat(question);
            Map<String, Object> data = new HashMap<>();
            data.put("answer", answer);
            data.put("model", "qwen-max");
            return ApiResponse.success(data, generateRequestId());
        } catch (Exception e) {
            logger.error("问答失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "问答失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 工地安全问答
     */
    @PostMapping("/construction")
    public ApiResponse<Map<String, Object>> constructionChat(@RequestParam String question) {
        logger.info("工地安全问答：question={}", question);
        try {
            String systemPrompt = "你是一位工地安全专家，熟悉建筑工地安全规范和操作规程。请根据安全知识回答问题，确保回答准确、专业、实用。";
            String answer = aiChatService.chatWithSystem(systemPrompt, question);
            Map<String, Object> data = new HashMap<>();
            data.put("answer", answer);
            data.put("scene", "工地安全");
            return ApiResponse.success(data, generateRequestId());
        } catch (Exception e) {
            logger.error("问答失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "问答失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 工业规范问答
     */
    @PostMapping("/industrial")
    public ApiResponse<Map<String, Object>> industrialChat(@RequestParam String question) {
        logger.info("工业规范问答：question={}", question);
        try {
            String systemPrompt = "你是一位工业规范专家，熟悉各类工业生产标准和规范。请根据规范文档回答问题。";
            String answer = aiChatService.chatWithSystem(systemPrompt, question);
            Map<String, Object> data = new HashMap<>();
            data.put("answer", answer);
            data.put("scene", "工业规范");
            return ApiResponse.success(data, generateRequestId());
        } catch (Exception e) {
            logger.error("问答失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "问答失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 生成请求 ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
