package com.interview.coach.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deepseek")
public record DeepSeekProperties(
        String apiKey,
        String baseUrl,
        String model,
        Integer timeoutSeconds
) {
    public boolean configured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
