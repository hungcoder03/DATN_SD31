package com.main.datn_sd31.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "gemini")
public class GeminiConfig {
    
    private String apiKey;
    private String modelName = "gemini-pro";
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta/models";
    private Integer maxTokens = 1000;
    private Double temperature = 0.7;
    private Integer requestTimeout = 30000; // 30 seconds
} 