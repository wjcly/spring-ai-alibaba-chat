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
 * OpenAI 兼容适配器（用于腾讯混元、字节豆包、OpenAI 等）
 */
public class OpenAiCompatibleAdapter implements AiProviderAdapter {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiCompatibleAdapter.class);

    private final String providerName;
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public OpenAiCompatibleAdapter(String providerName, String apiKey, String model, String baseUrl) {
        this.providerName = providerName;
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    @Override
    public AiProvider getProvider() {
        return AiProvider.OPENAI;
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
                    baseUrl + "/chat/completions", entity, String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "调用失败：" + e.getMessage();
        }
    }

    @Override
    public MultiModalResponse multimodalChat(MultiModalRequest request) {
        try {
            List<Map<String, Object>> messages = new ArrayList<>();
            List<Map<String, Object>> contentList = new ArrayList<>();

            if (request.getPrompt() != null) {
                Map<String, Object> textContent = new HashMap<>();
                textContent.put("type", "text");
                textContent.put("text", request.getPrompt());
                contentList.add(textContent);
            }
            if (request.getImageBase64() != null) {
                Map<String, Object> imageUrl = new HashMap<>();
                imageUrl.put("url", "data:image/jpeg;base64," + request.getImageBase64());
                Map<String, Object> imageContent = new HashMap<>();
                imageContent.put("type", "image_url");
                imageContent.put("image_url", imageUrl);
                contentList.add(imageContent);
            }

            Map<String, Object> userMessage;
            if (contentList.size() > 1) {
                userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", contentList);
            } else if (contentList.isEmpty()) {
                userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", request.getPrompt());
            } else {
                userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", contentList.get(0).get("text"));
            }
            messages.add(userMessage);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions", entity, String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String content = rootNode.path("choices").get(0).path("message").path("content").asText();

            return MultiModalResponse.builder()
                    .content(content)
                    .provider(providerName.toLowerCase())
                    .build();
        } catch (Exception e) {
            return MultiModalResponse.builder()
                    .content("调用失败：" + e.getMessage())
                    .provider(providerName.toLowerCase())
                    .build();
        }
    }

    @Override
    public String analyzeImage(InputStream imageStream, String prompt) {
        try {
            String imageData = Base64.getEncoder().encodeToString(imageStream.readAllBytes());
            MultiModalRequest request = MultiModalRequest.builder()
                    .prompt(prompt != null ? prompt : "请分析这张图片")
                    .imageBase64(imageData)
                    .build();
            return multimodalChat(request).getContent();
        } catch (Exception e) {
            return "图片分析失败：" + e.getMessage();
        }
    }

    @Override
    public String analyzeDocument(InputStream documentStream, String prompt) {
        return chat(prompt);
    }

    @Override
    public boolean supportsMultimodal() {
        return model.toLowerCase().contains("gpt-4") || model.toLowerCase().contains("vision");
    }

    @Override
    public boolean supportsEmbedding() {
        return true;
    }
}
