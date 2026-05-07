package com.interview.coach.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.coach.config.DeepSeekProperties;
import com.interview.coach.dto.EvaluationResult;
import com.interview.coach.dto.EvaluationResult.DimensionScore;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class DeepSeekClient {

    private static final List<String> DIMENSION_NAMES = List.of(
            "语言表达",
            "内容深入",
            "角度多元",
            "政务思维及个性亮点",
            "紧扣题意",
            "逻辑结构"
    );

    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public DeepSeekClient(DeepSeekProperties properties, ObjectMapper objectMapper, RestClient.Builder builder) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public EvaluationResult evaluate(String prompt) {
        if (!properties.configured()) {
            return EvaluationResult.fallback("DeepSeek API Key 未配置，无法调用模型评分。");
        }
        Map<String, Object> payload = Map.of(
                "model", properties.model(),
                "messages", List.of(
                        Map.of("role", "system", "content", "你是结构化面试评分专家，只返回合法 JSON。"),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object")
        );
        try {
            String response = restClient.post()
                    .uri("/chat/completions")
                    .headers(headers -> headers.setBearerAuth(properties.apiKey()))
                    .body(payload)
                    .retrieve()
                    .body(String.class);
            return parseEvaluation(response);
        } catch (Exception e) {
            return EvaluationResult.fallback("AI 评分服务调用失败：" + e.getMessage());
        }
    }

    private EvaluationResult parseEvaluation(String response) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(response);
        String content = root.path("choices").path(0).path("message").path("content").asText();
        if (content == null || content.isBlank()) {
            content = response;
        }

        EvaluationResult result = objectMapper.readValue(stripCodeFence(content), EvaluationResult.class);
        int normalizedScore = normalize(result.score(), 0, 100);
        return new EvaluationResult(
                normalizedScore,
                defaultText(result.level(), levelOf(normalizedScore)),
                defaultText(result.questionType(), "未明确判断"),
                defaultText(result.questionTypeReason(), "模型未给出题型依据。"),
                result.answerDurationSeconds(),
                defaultText(result.durationComment(), "建议结构化面试作答控制在 2 到 3 分钟。"),
                normalizeDimensions(result.dimensionScores()),
                defaultText(result.scoreExplanation(), "模型未给出总评。"),
                defaultText(result.scoreGapAssessment(), "模型未判断分差表现。"),
                safeList(result.majorDeductions()),
                safeList(result.examinerHighlights()),
                safeList(result.strengths()),
                safeList(result.weaknesses()),
                safeList(result.examinerPerspective()),
                safeList(result.sentenceLevelProblems()),
                safeList(result.priorityImprovements()),
                defaultText(result.contentAdvice(), "请围绕题干关键词补充原因、影响和对策。"),
                defaultText(result.structureAdvice(), "建议使用“表态、分析、对策、升华”的结构。"),
                defaultText(result.expressionAdvice(), "建议减少口语化表达，使用更规范的短句。"),
                safeList(result.answerFramework()),
                safeList(result.goldenSentences()),
                safeList(result.optimizedAnswer()),
                safeList(result.sampleAnswer()),
                safeList(result.memorizationOutline()),
                safeList(result.deliveryAdvice()),
                safeList(result.transferableScenarios()),
                safeList(result.sampleAnswerOutline())
        );
    }

    private String stripCodeFence(String content) {
        return content.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "").trim();
    }

    private String levelOf(int score) {
        if (score >= 90) {
            return "高分答案";
        }
        if (score >= 82) {
            return "优秀";
        }
        if (score >= 72) {
            return "中上";
        }
        if (score >= 60) {
            return "中等";
        }
        return "较差";
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private int normalize(Integer score, int min, int max) {
        return Math.max(min, Math.min(max, score == null ? min : score));
    }

    private List<DimensionScore> normalizeDimensions(List<DimensionScore> dimensions) {
        return DIMENSION_NAMES.stream()
                .map(name -> dimensions == null ? new DimensionScore(name, 0, "模型未返回该维度评分") : dimensions.stream()
                        .filter(item -> name.equals(item.name()))
                        .findFirst()
                        .map(item -> new DimensionScore(
                                name,
                                normalize(item.score(), 0, 10),
                                defaultText(item.comment(), "暂无评价")
                        ))
                        .orElseGet(() -> new DimensionScore(name, 0, "模型未返回该维度评分")))
                .toList();
    }
}
