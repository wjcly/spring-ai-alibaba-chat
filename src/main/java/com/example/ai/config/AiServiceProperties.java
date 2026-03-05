package com.example.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 服务配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.service")
public class AiServiceProperties {

    private RateLimitConfig rateLimit = new RateLimitConfig();
    private RetryConfig retry = new RetryConfig();
    private TimeoutConfig timeout = new TimeoutConfig();
    private RagConfig rag = new RagConfig();

    @Data
    public static class RateLimitConfig {
        private boolean enabled = true;
        private int requestsPerSecond = 10;
    }

    @Data
    public static class RetryConfig {
        private boolean enabled = true;
        private int maxAttempts = 3;
        private long backoffDelay = 1000;
    }

    @Data
    public static class TimeoutConfig {
        private int connection = 5000;
        private int read = 30000;
    }

    @Data
    public static class RagConfig {
        private boolean enabled = true;
        private double similarityThreshold = 0.7;
        private int maxDocuments = 5;
        private int chunkSize = 500;
        private int chunkOverlap = 50;
    }
}
