package com.example.ai.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 厂商枚举
 */
@Getter
@AllArgsConstructor
public class AiProvider {

    /**
     * 阿里云百炼 (Qwen/通义千问)
     * API: https://help.aliyun.com/zh/dashscope/
     */
    public static final AiProvider ALIBABA = new AiProvider("alibaba", "阿里云百炼", "Qwen-Max", "https://dashscope.aliyuncs.com");

    /**
     * DeepSeek (深度求索)
     * API: https://platform.deepseek.com/
     */
    public static final AiProvider DEEPSEEK = new AiProvider("deepseek", "DeepSeek", "deepseek-chat", "https://api.deepseek.com");

    /**
     * 腾讯混元
     * API: https://cloud.tencent.com/product/hunyuan
     */
    public static final AiProvider TENCENT = new AiProvider("tencent", "腾讯混元", "hunyuan-lite", "https://hunyuan.tencentcloudapi.com");

    /**
     * 字节豆包
     * API: https://www.volcengine.com/product/doubao
     */
    public static final AiProvider DOUBAO = new AiProvider("doubao", "字节豆包", "Doubao-pro-4k", "https://ark.cn-beijing.volces.com");

    /**
     * OpenAI (ChatGPT/GPT-4)
     * API: https://platform.openai.com/
     */
    public static final AiProvider OPENAI = new AiProvider("openai", "OpenAI", "gpt-4-turbo", "https://api.openai.com");

    /**
     * Azure OpenAI
     * API: https://azure.microsoft.com/zh-cn/products/ai-services/openai-service
     */
    public static final AiProvider AZURE_OPENAI = new AiProvider("azure-openai", "Azure OpenAI", "gpt-4", "");

    /**
     * NVIDIA NIM / NeMo
     * API: https://build.nvidia.com/
     */
    public static final AiProvider NVIDIA = new AiProvider("nvidia", "NVIDIA NIM", "meta/llama3-70b", "https://integrate.api.nvidia.com");

    /**
     * Ollama (本地部署)
     * API: https://ollama.ai/
     */
    public static final AiProvider OLLAMA = new AiProvider("ollama", "Ollama", "llama3", "http://localhost:11434");

    /**
     * 360 智脑
     * API: https://ai.360.cn/
     */
    public static final AiProvider AI360 = new AiProvider("360", "360 智脑", "360gpt2-pro", "https://api.360.cn");

    /**
     * 百度文心一言
     * API: https://cloud.baidu.com/product/wenxinworkshop
     */
    public static final AiProvider BAIDU = new AiProvider("baidu", "百度文心", "ernie-bot-4", "https://aip.baidubce.com");

    private final String code;
    private final String name;
    private final String defaultModel;
    private final String baseUrl;

    public static AiProvider fromCode(String code) {
        if (code == null) return ALIBABA;
        String lowerCode = code.toLowerCase();
        if ("alibaba".equals(lowerCode)) return ALIBABA;
        if ("deepseek".equals(lowerCode)) return DEEPSEEK;
        if ("tencent".equals(lowerCode)) return TENCENT;
        if ("doubao".equals(lowerCode)) return DOUBAO;
        if ("openai".equals(lowerCode)) return OPENAI;
        if ("azure-openai".equals(lowerCode) || "azure".equals(lowerCode)) return AZURE_OPENAI;
        if ("nvidia".equals(lowerCode)) return NVIDIA;
        if ("ollama".equals(lowerCode)) return OLLAMA;
        if ("360".equals(lowerCode)) return AI360;
        if ("baidu".equals(lowerCode)) return BAIDU;
        return ALIBABA;
    }
}
