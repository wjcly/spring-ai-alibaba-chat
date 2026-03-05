package com.example.ai.adapter;

import com.example.ai.adapter.impl.*;
import com.example.ai.config.MultiProviderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 厂商适配器管理器
 */
@Component
public class AiProviderAdapterManager {

    private static final Logger logger = LoggerFactory.getLogger(AiProviderAdapterManager.class);

    private final Map<String, AiProviderAdapter> adapters = new ConcurrentHashMap<>();
    private final MultiProviderProperties properties;
    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;

    private String defaultProvider = "alibaba";

    public AiProviderAdapterManager(MultiProviderProperties properties,
                                   ChatClient chatClient,
                                   EmbeddingModel embeddingModel) {
        this.properties = properties;
        this.chatClient = chatClient;
        this.embeddingModel = embeddingModel;
        this.defaultProvider = properties.getDefaultProvider();
        registerAllAdapters();
    }

    private void registerAllAdapters() {
        // 阿里云百炼 - 手动创建
        if (properties.getAlibaba().isEnabled()) {
            adapters.put("alibaba", new AlibabaAiAdapter(
                    chatClient,
                    embeddingModel));
            logger.info("注册阿里云 AI 适配器");
        }
        
        // DeepSeek - 使用 HTTP
        if (properties.getDeepseek().isEnabled()) {
            adapters.put("deepseek", new DeepSeekAiAdapter(
                    properties.getDeepseek().getApiKey(),
                    properties.getDeepseek().getModel(),
                    properties.getDeepseek().getBaseUrl()));
            logger.info("注册 DeepSeek AI 适配器");
        }

        // 腾讯混元 - 使用 OpenAI 兼容接口
        if (properties.getTencent().isEnabled()) {
            adapters.put("tencent", new OpenAiCompatibleAdapter(
                    "Tencent Hunyuan",
                    properties.getTencent().getApiKey(),
                    properties.getTencent().getModel(),
                    properties.getTencent().getBaseUrl()));
            logger.info("注册腾讯混元 AI 适配器");
        }

        // 字节豆包 - 使用 OpenAI 兼容接口
        if (properties.getDoubao().isEnabled()) {
            adapters.put("doubao", new OpenAiCompatibleAdapter(
                    "Doubao",
                    properties.getDoubao().getApiKey(),
                    properties.getDoubao().getModel(),
                    properties.getDoubao().getBaseUrl()));
            logger.info("注册字节豆包 AI 适配器");
        }

        // OpenAI - 使用 Spring AI 或 HTTP
        if (properties.getOpenai().isEnabled()) {
            adapters.put("openai", new OpenAiCompatibleAdapter(
                    "OpenAI",
                    properties.getOpenai().getApiKey(),
                    properties.getOpenai().getModel(),
                    properties.getOpenai().getBaseUrl()));
            logger.info("注册 OpenAI AI 适配器");
        }

        // NVIDIA - 使用 HTTP
        if (properties.getNvidia().isEnabled()) {
            adapters.put("nvidia", new NvidiaAiAdapter(
                    properties.getNvidia().getApiKey(),
                    properties.getNvidia().getModel(),
                    properties.getNvidia().getBaseUrl()));
            logger.info("注册 NVIDIA AI 适配器");
        }

        // Ollama - 本地部署
        if (properties.getOllama().isEnabled()) {
            adapters.put("ollama", new OpenAiCompatibleAdapter(
                    "Ollama",
                    properties.getOllama().getApiKey(),
                    properties.getOllama().getModel(),
                    properties.getOllama().getBaseUrl()));
            logger.info("注册 Ollama AI 适配器");
        }

        logger.info("已注册 {} 个 AI 适配器", adapters.size());
    }

    public AiProviderAdapter getAdapter(String providerCode) {
        AiProviderAdapter adapter = adapters.get(providerCode.toLowerCase());
        if (adapter == null) {
            logger.warn("未找到厂商 {} 的适配器，使用默认厂商", providerCode);
            return getDefaultAdapter();
        }
        return adapter;
    }

    public AiProviderAdapter getDefaultAdapter() {
        return getAdapter(defaultProvider);
    }

    public void setDefaultProvider(String providerCode) {
        if (adapters.containsKey(providerCode.toLowerCase())) {
            this.defaultProvider = providerCode.toLowerCase();
            logger.info("设置默认 AI 厂商为：{}", providerCode);
        }
    }

    public List<String> getAvailableProviders() {
        return new ArrayList<>(adapters.keySet());
    }

    public boolean isProviderAvailable(String providerCode) {
        return adapters.containsKey(providerCode.toLowerCase());
    }

    public Map<String, String> broadcastChat(String prompt) {
        Map<String, String> results = new HashMap<>();
        for (Map.Entry<String, AiProviderAdapter> entry : adapters.entrySet()) {
            try {
                String response = entry.getValue().chat(prompt);
                results.put(entry.getKey(), response);
            } catch (Exception e) {
                results.put(entry.getKey(), "错误：" + e.getMessage());
            }
        }
        return results;
    }
}
