package com.interview.coach.controller;

import com.interview.coach.dto.EvaluationRequest;
import com.interview.coach.dto.EvaluationResult;
import com.interview.coach.service.EvaluationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    public EvaluationResult evaluate(@Valid @RequestBody EvaluationRequest request) {
        return evaluationService.evaluate(request.questionId(), request.answer());
        return evaluationService.evaluate(request.questionId(), request.answer(), request.answerDurationSeconds());
    }
}
