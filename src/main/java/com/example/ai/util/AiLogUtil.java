package com.example.ai.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 日志工具类
 * 用于日志埋点和参数脱敏
 */
@UtilityClass
public class AiLogUtil {

    private static final Logger logger = LoggerFactory.getLogger(AiLogUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 参数脱敏处理
     */
    public static String sanitizeParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        Map<String, Object> sanitized = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String key = "arg" + i;
            Object arg = args[i];
            
            // 脱敏敏感信息
            if (arg instanceof String) {
                String strArg = (String) arg;
                if (strArg.toLowerCase().contains("key") || 
                    strArg.toLowerCase().contains("token") ||
                    strArg.toLowerCase().contains("secret") ||
                    strArg.toLowerCase().contains("password")) {
                    sanitized.put(key, "***SENSITIVE***");
                } else {
                    sanitized.put(key, truncate(strArg, 100));
                }
            } else {
                try {
                    sanitized.put(key, objectMapper.writeValueAsString(arg));
                } catch (Exception e) {
                    sanitized.put(key, arg != null ? arg.toString() : "null");
                }
            }
        }

        try {
            return objectMapper.writeValueAsString(sanitized);
        } catch (Exception e) {
            return sanitized.toString();
        }
    }

    /**
     * 字符串截断
     */
    public static String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() <= maxLength ? str : str.substring(0, maxLength) + "...";
    }

    /**
     * 记录请求指标
     */
    public static void logMetrics(String operation, long durationMs, boolean success) {
        logger.info("METRIC|operation={}|duration_ms={}|success={}", 
                operation, durationMs, success);
    }
}
