package com.interview.coach.controller;

import com.interview.coach.domain.ExamType;
import com.interview.coach.dto.QuestionResponse;
import com.interview.coach.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/random")
    public ResponseEntity<QuestionResponse> random(@RequestParam(required = false) Integer year,
                                                   @RequestParam(required = false) ExamType type) {
        return questionService.randomQuestion(year, type)
                .map(QuestionResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/years")
    public List<Integer> years() {
        return questionService.years();
    }
}
