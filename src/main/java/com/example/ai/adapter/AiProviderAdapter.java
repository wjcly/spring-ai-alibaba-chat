package com.example.ai.adapter;

import com.example.ai.model.AiProvider;
import com.example.ai.model.MultiModalRequest;
import com.example.ai.model.MultiModalResponse;

import java.io.InputStream;

/**
 * AI 厂商适配器接口
 */
public interface AiProviderAdapter {

    AiProvider getProvider();

    String chat(String prompt);

    MultiModalResponse multimodalChat(MultiModalRequest request);

    String analyzeImage(InputStream imageStream, String prompt);

    String analyzeDocument(InputStream documentStream, String prompt);

    boolean supportsMultimodal();

    boolean supportsEmbedding();
}
