package com.example.ai.adapter.impl;

import com.example.ai.adapter.AiProviderAdapter;
import com.example.ai.model.AiProvider;
import com.example.ai.model.MultiModalRequest;
import com.example.ai.model.MultiModalResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.*;

/**
 * NVIDIA NIM 适配器
 */
public class NvidiaAiAdapter implements AiProviderAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NvidiaAiAdapter.class);

    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public NvidiaAiAdapter(String apiKey, String model, String baseUrl) {
        this.apiKey = apiKey;
        this.model = model != null ? model : "meta/llama3-70b";
        this.baseUrl = baseUrl != null ? baseUrl : "https://integrate.api.nvidia.com";
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    @Override
    public AiProvider getProvider() {
        return AiProvider.NVIDIA;
    }

    @Override
    public String chat(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/v1/chat/completions", entity, String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "调用失败：" + e.getMessage();
        }
    }

    @Override
    public MultiModalResponse multimodalChat(MultiModalRequest request) {
        return MultiModalResponse.builder()
                .content(chat(request.getPrompt()))
                .provider("nvidia")
                .build();
    }

    @Override
    public String analyzeImage(InputStream imageStream, String prompt) {
        return "NVIDIA 视觉分析需要配置视觉模型";
    }

    @Override
    public String analyzeDocument(InputStream documentStream, String prompt) {
        return chat(prompt);
    }

    @Override
    public boolean supportsMultimodal() {
        return false;
    }

    @Override
    public boolean supportsEmbedding() {
        return true;
    }
}
