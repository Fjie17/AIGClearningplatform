package com.example.learningplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AiConfig {

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.api-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String apiUrl;

    @Value("${ai.model:qwen-max}")
    private String model;

    @Value("${ai.temperature:0.1}")
    private Double temperature;

    @Value("${ai.max-tokens:500}")
    private Integer maxTokens;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getModel() {
        return model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }
}