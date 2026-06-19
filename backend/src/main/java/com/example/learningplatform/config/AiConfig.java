package com.example.learningplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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

    @Value("${ai.match-version:1.0}")
    private String matchVersion;

    @Value("${ai.url-connect-timeout-ms:15000}")
    private int urlConnectTimeoutMs;

    @Value("${ai.url-read-timeout-ms:60000}")
    private int urlReadTimeoutMs;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(urlConnectTimeoutMs);
        factory.setReadTimeout(urlReadTimeoutMs);
        return new RestTemplate(factory);
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

    public String getMatchVersion() {
        return matchVersion;
    }

    /**
     * 拼接 Chat Completions 完整 URL，兼容带/不带尾斜杠、已含 chat/completions 的配置。
     */
    public String getChatCompletionsUrl() {
        String base = apiUrl == null ? "" : apiUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base.endsWith("/chat/completions")) {
            return base;
        }
        return base + "/chat/completions";
    }
}