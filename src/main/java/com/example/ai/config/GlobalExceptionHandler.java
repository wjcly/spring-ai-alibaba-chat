package com.example.ai.config;

import com.example.ai.model.ApiResponse;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(400)
                .message("参数校验失败")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .requestId(generateRequestId())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理限流异常
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitException(
            RequestNotPermitted ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(429)
                .message("请求频率过高，请稍后重试")
                .timestamp(LocalDateTime.now())
                .requestId(generateRequestId())
                .build();

        return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        logger.warn("非法参数：{}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(400)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .requestId(generateRequestId())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        logger.error("服务器内部错误：{}", ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(500)
                .message("服务器内部错误：" + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .requestId(generateRequestId())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 生成请求 ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
