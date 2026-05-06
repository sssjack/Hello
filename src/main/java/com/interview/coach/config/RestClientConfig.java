package com.interview.coach.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    RestClientCustomizer deepSeekTimeoutCustomizer(DeepSeekProperties properties) {
        return builder -> {
            int timeoutSeconds = properties.timeoutSeconds() == null ? 60 : properties.timeoutSeconds();
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(Duration.ofSeconds(10));
            factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
            builder.requestFactory(factory);
        };
    }
}
