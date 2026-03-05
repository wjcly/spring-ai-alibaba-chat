package com.example.ai.controller;

import com.example.ai.adapter.AiProviderAdapter;
import com.example.ai.adapter.AiProviderAdapterManager;
import com.example.ai.model.ApiResponse;
import com.example.ai.model.MultiModalRequest;
import com.example.ai.model.MultiModalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 多 AI 厂商管理 API 接口
 */
@RestController
@RequestMapping("/ai")
public class AiProviderController {

    private static final Logger logger = LoggerFactory.getLogger(AiProviderController.class);

    private final AiProviderAdapterManager adapterManager;

    public AiProviderController(AiProviderAdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

    /**
     * 获取可用的 AI 厂商列表
     */
    @GetMapping("/providers")
    public ApiResponse<List<String>> getAvailableProviders() {
        List<String> providers = adapterManager.getAvailableProviders();
        return ApiResponse.success(providers, generateRequestId());
    }

    /**
     * 设置默认 AI 厂商
     */
    @PostMapping("/providers/default")
    public ApiResponse<String> setDefaultProvider(
            @RequestParam String provider) {
        logger.info("设置默认 AI 厂商：{}", provider);
        adapterManager.setDefaultProvider(provider);
        return ApiResponse.success("设置成功，当前默认厂商：" + provider, generateRequestId());
    }

    /**
     * 获取当前默认厂商
     */
    @GetMapping("/providers/default")
    public ApiResponse<String> getDefaultProvider() {
        AiProviderAdapter adapter = adapterManager.getDefaultAdapter();
        return ApiResponse.success(adapter.getProvider().getCode(), generateRequestId());
    }

    /**
     * 统一对话接口 (可指定厂商)
     */
    @PostMapping("/chat")
    public ApiResponse<MultiModalResponse> chat(
            @RequestBody ChatRequest request) {
        logger.info("AI 对话：provider={}, prompt={}", request.getProvider(), request.getPrompt());

        try {
            AiProviderAdapter adapter = request.getProvider() != null ?
                    adapterManager.getAdapter(request.getProvider()) :
                    adapterManager.getDefaultAdapter();

            String response = adapter.chat(request.getPrompt());

            MultiModalResponse multimodalResponse = MultiModalResponse.builder()
                    .content(response)
                    .provider(adapter.getProvider().getCode())
                    .build();

            return ApiResponse.success(multimodalResponse, generateRequestId());
        } catch (Exception e) {
            logger.error("AI 对话失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "AI 对话失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 多模态对话 (图文混合)
     */
    @PostMapping("/multimodal")
    public ApiResponse<MultiModalResponse> multimodalChat(
            @RequestBody MultiModalRequest request) {
        logger.info("多模态对话：provider={}", request);

        try {
            AiProviderAdapter adapter = adapterManager.getDefaultAdapter();

            if (!adapter.supportsMultimodal()) {
                String response = adapter.chat(request.getPrompt());
                return ApiResponse.success(MultiModalResponse.builder()
                        .content(response)
                        .provider(adapter.getProvider().getCode())
                        .build(), generateRequestId());
            }

            MultiModalResponse response = adapter.multimodalChat(request);
            return ApiResponse.success(response, generateRequestId());
        } catch (Exception e) {
            logger.error("多模态对话失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "多模态对话失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 广播测试 (同时调用所有厂商)
     */
    @PostMapping("/broadcast")
    public ApiResponse<Map<String, String>> broadcastChat(
            @RequestParam @NotBlank String prompt) {
        logger.info("广播测试：prompt={}", prompt);

        try {
            Map<String, String> results = adapterManager.broadcastChat(prompt);
            return ApiResponse.success(results, generateRequestId());
        } catch (Exception e) {
            logger.error("广播测试失败：{}", e.getMessage(), e);
            return ApiResponse.error(500, "广播测试失败：" + e.getMessage(), generateRequestId());
        }
    }

    /**
     * 厂商状态检查
     */
    @GetMapping("/providers/{provider}/status")
    public ApiResponse<Map<String, Object>> checkProviderStatus(
            @PathVariable String provider) {
        logger.info("检查厂商状态：{}", provider);

        Map<String, Object> status = new HashMap<>();
        status.put("provider", provider);
        status.put("available", adapterManager.isProviderAvailable(provider));

        if (adapterManager.isProviderAvailable(provider)) {
            AiProviderAdapter adapter = adapterManager.getAdapter(provider);
            status.put("supportsMultimodal", adapter.supportsMultimodal());
            status.put("supportsEmbedding", adapter.supportsEmbedding());
        }

        return ApiResponse.success(status, generateRequestId());
    }

    /**
     * 切换厂商对话示例
     */
    @PostMapping("/compare")
    public ApiResponse<Map<String, String>> compareProviders(
            @RequestBody CompareRequest request) {
        logger.info("对比厂商：prompts={}", request.getPrompts());

        Map<String, String> results = new HashMap<>();

        for (String provider : request.getProviders()) {
            try {
                AiProviderAdapter adapter = adapterManager.getAdapter(provider);
                String prompt = request.getPrompts() != null && request.getPrompts().containsKey(provider) 
                    ? request.getPrompts().get(provider) 
                    : request.getDefaultPrompt();
                String response = adapter.chat(prompt);
                results.put(provider, response);
            } catch (Exception e) {
                results.put(provider, "错误：" + e.getMessage());
            }
        }

        return ApiResponse.success(results, generateRequestId());
    }

    /**
     * 对话请求 (Java 8 兼容)
     */
    public static class ChatRequest {
        private String prompt;
        private String provider;
        private Double temperature;
        private Integer maxTokens;

        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
        public Integer getMaxTokens() { return maxTokens; }
        public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    }

    /**
     * 对比请求 (Java 8 兼容)
     */
    public static class CompareRequest {
        private List<String> providers;
        private String defaultPrompt;
        private Map<String, String> prompts;

        public List<String> getProviders() { return providers; }
        public void setProviders(List<String> providers) { this.providers = providers; }
        public String getDefaultPrompt() { return defaultPrompt; }
        public void setDefaultPrompt(String defaultPrompt) { this.defaultPrompt = defaultPrompt; }
        public Map<String, String> getPrompts() { return prompts; }
        public void setPrompts(Map<String, String> prompts) { this.prompts = prompts; }
    }

    /**
     * 生成请求 ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
