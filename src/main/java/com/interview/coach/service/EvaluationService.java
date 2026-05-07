package com.interview.coach.service;

import com.interview.coach.domain.ExamType;
import com.interview.coach.domain.Question;
import com.interview.coach.dto.EvaluationResult;
import com.interview.coach.repository.EvaluationRepository;
import org.springframework.stereotype.Service;

@Service
public class EvaluationService {

    private final QuestionService questionService;
    private final PromptBuilder promptBuilder;
    private final DeepSeekClient deepSeekClient;
    private final EvaluationRepository evaluationRepository;

    public EvaluationService(QuestionService questionService,
                             PromptBuilder promptBuilder,
                             DeepSeekClient deepSeekClient,
                             EvaluationRepository evaluationRepository) {
        this.questionService = questionService;
        this.promptBuilder = promptBuilder;
        this.deepSeekClient = deepSeekClient;
        this.evaluationRepository = evaluationRepository;
    }

    public EvaluationResult evaluate(Long questionId, String customQuestion, String answer, Integer answerDurationSeconds) {
        String normalizedCustomQuestion = normalizeCustomQuestion(customQuestion);
        Question question = resolveQuestion(questionId, normalizedCustomQuestion);
        EvaluationResult result = ensureDuration(
                deepSeekClient.evaluate(promptBuilder.build(question, answer, answerDurationSeconds)),
                answerDurationSeconds
        );
        evaluationRepository.save(question.id(), normalizedCustomQuestion, answer, answerDurationSeconds, result);
        return result;
    }

    private Question resolveQuestion(Long questionId, String customQuestion) {
        if (customQuestion != null) {
            return new Question(
                    null,
                    null,
                    ExamType.CUSTOM,
                    "自定义",
                    "用户自定义题目",
                    "自定义题",
                    customQuestion,
                    null
            );
        }
        if (questionId == null) {
            throw new IllegalArgumentException("请先随机抽题或填写自定义题目");
        }
        return questionService.getQuestion(questionId);
    }

    private String normalizeCustomQuestion(String customQuestion) {
        if (customQuestion == null || customQuestion.isBlank()) {
            return null;
        }
        String normalized = customQuestion.trim();
        if (normalized.length() < 5) {
            throw new IllegalArgumentException("自定义题目内容过短");
        }
        return normalized;
    }

    private EvaluationResult ensureDuration(EvaluationResult result, Integer answerDurationSeconds) {
        if (result.answerDurationSeconds() != null || answerDurationSeconds == null) {
            return result;
        }
        return new EvaluationResult(
                result.score(),
                result.level(),
                result.questionType(),
                result.questionTypeReason(),
                answerDurationSeconds,
                result.durationComment(),
                result.dimensionScores(),
                result.scoreExplanation(),
                result.scoreGapAssessment(),
                result.majorDeductions(),
                result.examinerHighlights(),
                result.strengths(),
                result.weaknesses(),
                result.examinerPerspective(),
                result.sentenceLevelProblems(),
                result.priorityImprovements(),
                result.contentAdvice(),
                result.structureAdvice(),
                result.expressionAdvice(),
                result.answerFramework(),
                result.goldenSentences(),
                result.optimizedAnswer(),
                result.sampleAnswer(),
                result.memorizationOutline(),
                result.deliveryAdvice(),
                result.transferableScenarios(),
                result.sampleAnswerOutline()
        );
    }
}
