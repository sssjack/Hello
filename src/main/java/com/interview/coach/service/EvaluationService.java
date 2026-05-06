package com.interview.coach.service;

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

    public EvaluationResult evaluate(Long questionId, String answer) {
        Question question = questionService.getQuestion(questionId);
        EvaluationResult result = deepSeekClient.evaluate(promptBuilder.build(question, answer));
        evaluationRepository.save(questionId, answer, result);
        return result;
    }
    public EvaluationResult evaluate(Long questionId, String answer, Integer answerDurationSeconds) {
        Question question = questionService.getQuestion(questionId);
        EvaluationResult result = ensureDuration(
                deepSeekClient.evaluate(promptBuilder.build(question, answer, answerDurationSeconds)),
                answerDurationSeconds
        );
        evaluationRepository.save(questionId, answer, answerDurationSeconds, result);
        return result;
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
