package com.example.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring AI 配置类
 * 使用 @Qualifier 指定使用哪个厂商的 ChatModel
 */
@Configuration
public class AiConfig {

    /**
     * 配置 ChatClient.Builder（用于其他服务注入）
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel dashscopeChatModel) {
        return ChatClient.builder(dashscopeChatModel);
    }

    /**
     * 配置默认的 ChatClient（使用阿里云 DashScope）
     */
    @Bean
    @Primary
    public ChatClient chatClient(ChatModel dashscopeChatModel) {
        return ChatClient.builder(dashscopeChatModel).build();
    }

    /**
     * 配置向量存储（使用阿里云的 EmbeddingModel）
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        return new SimpleVectorStore(dashscopeEmbeddingModel);
    }
}
