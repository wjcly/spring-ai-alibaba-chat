package com.example.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 基于 HTTP 的 AI 对话服务
 * 直接调用阿里云百炼 API
 */
@Service
public class AiChatService {

    private static final Logger logger = LoggerFactory.getLogger(AiChatService.class);

    @Value("${ai.providers.alibaba.api-key:}")
    private String apiKey;

    @Value("${ai.providers.alibaba.model:qwen-max}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 简单对话
     */
    public String chat(String prompt) {
        logger.info("AI 对话：prompt={}", prompt.substring(0, Math.min(20, prompt.length())));

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("input", Map.of("messages", List.of(
                    Map.of("role", "user", "content", prompt)
            )));
            requestBody.put("parameters", Map.of("result_format", "message"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("X-DashScope-SSE", "disable");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation",
                    entity,
                    Map.class);

            Map body = response.getBody();
            if (body != null && body.containsKey("output")) {
                Map output = (Map) body.get("output");
                if (output.containsKey("choices")) {
                    List choices = (List) output.get("choices");
                    if (!choices.isEmpty()) {
                        Map choice = (Map) choices.get(0);
                        if (choice.containsKey("message")) {
                            Map message = (Map) choice.get("message");
                            return (String) message.get("content");
                        }
                    }
                }
            }

            return "未获取到响应";

        } catch (Exception e) {
            logger.error("AI 对话失败：{}", e.getMessage(), e);
            return "调用失败：" + e.getMessage();
        }
    }

    /**
     * 带系统提示词的对话
     */
    public String chatWithSystem(String systemPrompt, String userPrompt) {
        logger.info("AI 对话（带系统提示）：system={}, user={}", 
                systemPrompt.substring(0, Math.min(20, systemPrompt.length())),
                userPrompt.substring(0, Math.min(20, userPrompt.length())));

        try {
            List<Map<String, String>> messages = new ArrayList<>();
            
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                messages.add(Map.of("role", "system", "content", systemPrompt));
            }
            messages.add(Map.of("role", "user", "content", userPrompt));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("input", Map.of("messages", messages));
            requestBody.put("parameters", Map.of("result_format", "message"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("X-DashScope-SSE", "disable");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation",
                    entity,
                    Map.class);

            Map body = response.getBody();
            if (body != null && body.containsKey("output")) {
                Map output = (Map) body.get("output");
                if (output.containsKey("choices")) {
                    List choices = (List) output.get("choices");
                    if (!choices.isEmpty()) {
                        Map choice = (Map) choices.get(0);
                        if (choice.containsKey("message")) {
                            Map message = (Map) choice.get("message");
                            return (String) message.get("content");
                        }
                    }
                }
            }

            return "未获取到响应";

        } catch (Exception e) {
            logger.error("AI 对话失败：{}", e.getMessage(), e);
            return "调用失败：" + e.getMessage();
        }
    }
}
