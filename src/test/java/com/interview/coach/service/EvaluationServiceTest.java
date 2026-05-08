package com.interview.coach.service;

import com.interview.coach.domain.ExamType;
import com.interview.coach.domain.Question;
import com.interview.coach.dto.EvaluationResult;
import com.interview.coach.repository.EvaluationRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EvaluationServiceTest {

    private final QuestionService questionService = mock(QuestionService.class);
    private final PromptBuilder promptBuilder = mock(PromptBuilder.class);
    private final DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);
    private final EvaluationRepository evaluationRepository = mock(EvaluationRepository.class);
    private final EvaluationService evaluationService = new EvaluationService(
            questionService,
            promptBuilder,
            deepSeekClient,
            evaluationRepository
    );

    @Test
    void evaluatesCustomQuestionWithoutLoadingQuestionBank() {
        EvaluationResult result = resultWithDuration(null);
        when(promptBuilder.build(any(Question.class), eq("我的作答"), eq(88))).thenReturn("prompt");
        when(deepSeekClient.evaluate("prompt")).thenReturn(result);

        evaluationService.evaluate(null, "请谈谈你对基层服务创新的理解。", "我的作答", 88);

        verify(questionService, never()).getQuestion(any());
        verify(evaluationRepository).save(
                eq(null),
                eq("请谈谈你对基层服务创新的理解。"),
                eq("我的作答"),
                eq(88),
                any(EvaluationResult.class)
        );
    }

    @Test
    void evaluatesQuestionBankQuestionWhenNoCustomQuestionIsProvided() {
        Question question = new Question(12L, 2026, ExamType.INSTITUTION, "全国", "题库", "综合分析", "题库题目", null);
        EvaluationResult result = resultWithDuration(70);
        when(questionService.getQuestion(12L)).thenReturn(question);
        when(promptBuilder.build(question, "我的作答", 70)).thenReturn("prompt");
        when(deepSeekClient.evaluate("prompt")).thenReturn(result);

        evaluationService.evaluate(12L, null, "我的作答", 70);

        verify(questionService).getQuestion(12L);
        verify(evaluationRepository).save(
                eq(12L),
                eq(null),
                eq("我的作答"),
                eq(70),
                eq(result)
        );
    }

    private EvaluationResult resultWithDuration(Integer duration) {
        return new EvaluationResult(
                80,
                "中上",
                "综合分析",
                "题干要求谈理解",
                duration,
                "用时合理",
                List.of(),
                "总评",
                "能拉开",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "内容建议",
                "结构建议",
                "表达建议",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
