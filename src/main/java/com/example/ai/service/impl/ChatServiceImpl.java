package com.example.ai.service.impl;

import com.example.ai.adapter.AiProviderAdapter;
import com.example.ai.adapter.AiProviderAdapterManager;
import com.example.ai.config.AiServiceProperties;
import com.example.ai.model.ChatRequest;
import com.example.ai.model.ChatResponse;
import com.example.ai.model.ChatResponse.ReferenceDocument;
import com.example.ai.model.ChatResponse.TokenUsage;
import com.example.ai.service.ChatService;
import com.example.ai.service.RagService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 智能问答服务实现
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatClient chatClient;
    private final RagService ragService;
    private final AiServiceProperties properties;
    private final RateLimiter rateLimiter;
    private final Retry retry;
    private final AiProviderAdapterManager adapterManager;

    private final Map<String, List<Message>> conversationStore = new ConcurrentHashMap<>();

    public ChatServiceImpl(ChatClient chatClient,
                          RagService ragService,
                          AiServiceProperties properties,
                          RateLimiter rateLimiter,
                          Retry retry,
                          AiProviderAdapterManager adapterManager) {
        this.chatClient = chatClient;
        this.ragService = ragService;
        this.properties = properties;
        this.rateLimiter = rateLimiter;
        this.retry = retry;
        this.adapterManager = adapterManager;
    }

    @Override
    public ChatResponse chat(String question) {
        ChatRequest request = ChatRequest.builder()
                .question(question)
                .useRag(true)
                .build();
        return chat(request);
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        String conversationId = request.getConversationId() != null ?
                request.getConversationId() : UUID.randomUUID().toString();

        if (properties.getRateLimit().isEnabled()) {
            rateLimiter.acquirePermission();
        }

        String context = "";
        List<ReferenceDocument> references = new ArrayList<>();

        // 获取指定的 AI 厂商适配器
        String provider = request.getProvider();
        AiProviderAdapter adapter = (provider != null && adapterManager.isProviderAvailable(provider)) 
                ? adapterManager.getAdapter(provider) 
                : adapterManager.getDefaultAdapter();

        if (Boolean.TRUE.equals(request.getUseRag()) && properties.getRag().isEnabled()) {
            try {
                List<ReferenceDocument> docs = ragService.retrieveRelevantDocuments(
                        request.getQuestion(),
                        request.getScene(),
                        provider);
                if (!docs.isEmpty()) {
                    context = buildContextFromDocuments(docs);
                    references = docs;
                }
            } catch (Exception e) {
                logger.warn("RAG 检索失败，使用通用问答：{}", e.getMessage());
            }
        }

        List<Message> messages = new ArrayList<>();
        String systemPrompt = buildSystemPrompt(request.getSystemPrompt(), request.getScene(), context);
        messages.add(new SystemMessage(systemPrompt));

        if (conversationStore.containsKey(conversationId)) {
            messages.addAll(conversationStore.get(conversationId));
        }

        messages.add(new UserMessage(request.getQuestion()));

        String answer;
        if (properties.getRetry().isEnabled()) {
            answer = retry.executeSupplier(() ->
                chatClient.prompt(new Prompt(messages))
                    .call()
                    .content());
        } else {
            answer = chatClient.prompt(new Prompt(messages))
                    .call()
                    .content();
        }

        updateConversation(conversationId, messages, answer);

        long duration = System.currentTimeMillis() - startTime;

        return ChatResponse.builder()
                .answer(answer)
                .conversationId(conversationId)
                .model(adapter.getProvider().getDefaultModel())
                .references(references)
                .tokenUsage(TokenUsage.builder()
                        .promptTokens(messages.stream()
                                .mapToInt(m -> m.getContent().length() / 4)
                                .sum())
                        .completionTokens(answer.length() / 4)
                        .totalTokens((messages.stream()
                                .mapToInt(m -> m.getContent().length() / 4)
                                .sum()) + (answer.length() / 4))
                        .build())
                .durationMs(duration)
                .build();
    }

    @Override
    public ChatResponse chatWithConversation(String conversationId, String question) {
        ChatRequest request = ChatRequest.builder()
                .question(question)
                .conversationId(conversationId)
                .useRag(true)
                .build();
        return chat(request);
    }

    @Override
    public Flux<ChatResponse> streamChat(ChatRequest request) {
        return Flux.create(emitter -> {
            try {
                ChatResponse response = chat(request);
                emitter.next(response);
                emitter.complete();
            } catch (Exception e) {
                emitter.error(e);
            }
        });
    }

    private String buildSystemPrompt(String customPrompt, String scene, String context) {
        StringBuilder prompt = new StringBuilder();

        if ("工地安全".equals(scene) || "industrial".equals(scene)) {
            prompt.append("你是一位工地安全专家，熟悉建筑工地安全规范和操作规程。\n");
            prompt.append("请根据以下安全知识回答问题，确保回答准确、专业、实用。\n\n");
        } else if ("工业规范".equals(scene)) {
            prompt.append("你是一位工业规范专家，熟悉各类工业生产标准和规范。\n");
            prompt.append("请根据以下规范文档回答问题。\n\n");
        } else {
            prompt.append("你是一位智能助手，请根据以下参考信息回答问题。\n\n");
        }

        if (!context.isEmpty()) {
            prompt.append("【参考信息】\n");
            prompt.append(context);
            prompt.append("\n\n");
            prompt.append("请基于以上参考信息，结合你的知识，回答用户的问题。\n");
        }

        if (customPrompt != null && !customPrompt.isEmpty()) {
            prompt.append("\n").append(customPrompt);
        }

        return prompt.toString();
    }

    private String buildContextFromDocuments(List<ReferenceDocument> docs) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < docs.size(); i++) {
            ReferenceDocument doc = docs.get(i);
            context.append("[").append(i + 1).append("] ");
            context.append(doc.getContent());
            if (i < docs.size() - 1) {
                context.append("\n\n");
            }
        }
        return context.toString();
    }

    private void updateConversation(String conversationId, List<Message> messages, String answer) {
        List<Message> history = conversationStore.computeIfAbsent(conversationId, k -> new ArrayList<>());
        
        if (history.size() > 20) {
            history = new ArrayList<>(history.subList(history.size() - 20, history.size()));
        }
        
        history.add(messages.get(messages.size() - 1));
        history.add(new UserMessage(answer));
        
        conversationStore.put(conversationId, history);
    }
}
