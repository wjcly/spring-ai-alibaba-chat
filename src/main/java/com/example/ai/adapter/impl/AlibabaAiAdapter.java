package com.example.ai.adapter.impl;

import com.example.ai.adapter.AiProviderAdapter;
import com.example.ai.model.AiProvider;
import com.example.ai.model.MultiModalRequest;
import com.example.ai.model.MultiModalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.InputStream;
import java.util.Base64;

/**
 * 阿里云百炼 (Qwen) 适配器
 * 注意：不作为 @Component，由 AiProviderAdapterManager 手动管理
 */
public class AlibabaAiAdapter implements AiProviderAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AlibabaAiAdapter.class);

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final String model;

    public AlibabaAiAdapter(ChatClient chatClient,
                           @Qualifier("dashscopeEmbeddingModel") EmbeddingModel embeddingModel) {
        this.chatClient = chatClient;
        this.embeddingModel = embeddingModel;
        this.model = "qwen-vl-max";
    }

    @Override
    public AiProvider getProvider() {
        return AiProvider.ALIBABA;
    }

    @Override
    public String chat(String prompt) {
        logger.debug("阿里云 AI 对话");
        return chatClient.prompt(prompt).call().content();
    }

    @Override
    public MultiModalResponse multimodalChat(MultiModalRequest request) {
        logger.info("阿里云多模态对话");
        try {
            String response = chatClient.prompt(request.getPrompt()).call().content();
            return MultiModalResponse.builder()
                    .content(response)
                    .model(model)
                    .provider("alibaba")
                    .build();
        } catch (Exception e) {
            return MultiModalResponse.builder()
                    .content("调用失败：" + e.getMessage())
                    .provider("alibaba")
                    .build();
        }
    }

    @Override
    public String analyzeImage(InputStream imageStream, String prompt) {
        logger.info("分析图片");
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
        return chat(prompt != null ? prompt : "请分析这份文档");
    }

    @Override
    public boolean supportsMultimodal() {
        return true;
    }

    @Override
    public boolean supportsEmbedding() {
        return embeddingModel != null;
    }
}
