package com.example.ai.service;

import com.example.ai.model.ChatRequest;
import com.example.ai.model.ChatResponse;

/**
 * 智能问答服务接口
 */
public interface ChatService {

    /**
     * 简单问答
     * @param question 用户问题
     * @return AI 回答
     */
    ChatResponse chat(String question);

    /**
     * 带上下文的问答
     * @param request 问答请求
     * @return AI 回答
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 多轮对话
     * @param conversationId 会话 ID
     * @param question 用户问题
     * @return AI 回答
     */
    ChatResponse chatWithConversation(String conversationId, String question);

    /**
     * 流式问答 (SSE)
     * @param request 问答请求
     * @return 流式响应
     */
    reactor.core.publisher.Flux<ChatResponse> streamChat(ChatRequest request);
}
