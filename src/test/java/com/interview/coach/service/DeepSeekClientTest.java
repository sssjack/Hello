package com.interview.coach.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.coach.config.DeepSeekProperties;
import com.interview.coach.dto.EvaluationResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DeepSeekClientTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void evaluatesApplicationJsonChatCompletionResponse() throws Exception {
        startServer(200, """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\"score\\":86,\\"level\\":\\"优秀\\",\\"questionType\\":\\"综合分析\\",\\"questionTypeReason\\":\\"围绕观点理解展开\\",\\"answerDurationSeconds\\":95,\\"durationComment\\":\\"用时合理\\"}"
                      }
                    }
                  ]
                }
                """);

        EvaluationResult result = client().evaluate("prompt");

        assertThat(result.score()).isEqualTo(86);
        assertThat(result.level()).isEqualTo("优秀");
        assertThat(result.questionType()).isEqualTo("综合分析");
        assertThat(result.durationComment()).isEqualTo("用时合理");
    }

    @Test
    void returnsReadableFallbackForDeepSeekErrorJson() throws Exception {
        startServer(402, """
                {
                  "error": {
                    "message": "Insufficient Balance",
                    "type": "invalid_request_error"
                  }
                }
                """);

        EvaluationResult result = client().evaluate("prompt");

        assertThat(result.score()).isZero();
        assertThat(result.questionTypeReason()).contains("Insufficient Balance");
    }

    private DeepSeekClient client() {
        return new DeepSeekClient(
                new DeepSeekProperties("sk-test", baseUrl(), "deepseek-chat", 60),
                new ObjectMapper(),
                RestClient.builder()
        );
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private void startServer(int status, String responseBody) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat/completions", exchange -> respond(exchange, status, responseBody));
        server.start();
    }

    private void respond(HttpExchange exchange, int status, String responseBody) throws IOException {
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
