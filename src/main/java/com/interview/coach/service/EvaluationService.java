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
}
