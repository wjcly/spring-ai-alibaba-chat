package com.example.ai.config;

import com.example.ai.util.AiLogUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AI 服务日志切面
 * 记录请求参数、响应结果、执行时间等埋点信息
 */
@Aspect
@Component
public class AiLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(AiLogAspect.class);

    @Pointcut("execution(* com.example.ai.service..*.*(..)) && " +
              "!execution(* com.example.ai.service..*.*LogUtil*(..))")
    public void aiServiceMethods() {}

    @Around("aiServiceMethods()")
    public Object logAiService(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        
        // 记录请求开始
        long startTime = System.currentTimeMillis();
        logger.info("AI 服务调用开始 - {}.{} - 参数：{}", 
                className, methodName, AiLogUtil.sanitizeParams(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录请求成功
            logger.info("AI 服务调用完成 - {}.{} - 耗时：{}ms", className, methodName, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            // 记录请求失败
            logger.error("AI 服务调用失败 - {}.{} - 耗时：{}ms - 错误：{}", 
                    className, methodName, duration, e.getMessage(), e);
            throw e;
        }
    }
}
