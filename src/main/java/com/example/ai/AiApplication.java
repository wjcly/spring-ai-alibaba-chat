package com.example.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring AI 多厂商大模型集成平台启动类
 * 排除 ChatClient 自动配置，手动配置
 */
@SpringBootApplication(exclude = {
    org.springframework.ai.autoconfigure.chat.client.ChatClientAutoConfiguration.class
})
@EnableJpaRepositories(basePackages = "com.example.ai.repository")
@EntityScan(basePackages = "com.example.ai.model")
@ComponentScan(basePackages = "com.example.ai")
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }
}
