package com.interview.coach.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.coach.dto.EvaluationResult;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class EvaluationRepository {

    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;

    public EvaluationRepository(JdbcClient jdbcClient, ObjectMapper objectMapper) {
        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
    }

    public void save(Long questionId,
                     String customQuestion,
                     String answer,
                     Integer answerDurationSeconds,
                     EvaluationResult result) {
        try {
            jdbcClient.sql("""
                            insert into evaluations(question_id, custom_question, answer_text, answer_duration_seconds, score, level, result_json)
                            values (:questionId, :customQuestion, :answerText, :answerDurationSeconds, :score, :level, :resultJson)
                            """)
                    .param("questionId", questionId)
                    .param("customQuestion", customQuestion)
                    .param("answerText", answer)
                    .param("answerDurationSeconds", answerDurationSeconds)
                    .param("score", result.score())
                    .param("level", result.level())
                    .param("resultJson", objectMapper.writeValueAsString(result))
                    .update();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("评分结果序列化失败", e);
        }
    }
}
