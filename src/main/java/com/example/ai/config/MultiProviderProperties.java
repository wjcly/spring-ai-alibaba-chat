package com.example.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 多 AI 厂商配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.providers")
public class MultiProviderProperties {

    /**
     * 默认 AI 厂商
     */
    private String defaultProvider = "alibaba";

    /**
     * 阿里云百炼配置
     */
    private ProviderConfig alibaba = new ProviderConfig();

    /**
     * DeepSeek 配置（HTTP 调用）
     */
    private ProviderConfig deepseek = new ProviderConfig();

    /**
     * 腾讯混元配置（HTTP 调用）
     */
    private ProviderConfig tencent = new ProviderConfig();

    /**
     * 字节豆包配置（HTTP 调用）
     */
    private ProviderConfig doubao = new ProviderConfig();

    /**
     * OpenAI 配置（HTTP 调用）
     */
    private ProviderConfig openai = new ProviderConfig();

    /**
     * NVIDIA 配置（HTTP 调用）
     */
    private ProviderConfig nvidia = new ProviderConfig();

    /**
     * Ollama 配置（HTTP 调用）
     */
    private ProviderConfig ollama = new ProviderConfig();

    /**
     * 厂商配置
     */
    @Data
    public static class ProviderConfig {
        private String apiKey = "";
        private String apiSecret = "";
        private String baseUrl = "";
        private String model = "";
        private boolean enabled = false;
        private int timeout = 30000;
        private Map<String, String> extraConfig = new HashMap<>();
    }

    /**
     * 获取指定厂商的配置
     */
    public ProviderConfig getProvider(String providerCode) {
        String code = providerCode.toLowerCase();
        if ("alibaba".equals(code)) return alibaba;
        if ("deepseek".equals(code)) return deepseek;
        if ("tencent".equals(code)) return tencent;
        if ("doubao".equals(code)) return doubao;
        if ("openai".equals(code)) return openai;
        if ("nvidia".equals(code)) return nvidia;
        if ("ollama".equals(code)) return ollama;
        return alibaba;
    }
}
