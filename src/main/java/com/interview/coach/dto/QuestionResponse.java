package com.interview.coach.dto;

import com.interview.coach.domain.Question;

public record QuestionResponse(
        Long id,
        Integer year,
        String type,
        String typeLabel,
        String province,
        String source,
        String tags,
        String content
) {
    public static QuestionResponse from(Question question) {
        return new QuestionResponse(
                question.id(),
                question.examYear(),
                question.examType().name(),
                question.examType().getLabel(),
                question.province(),
                question.source(),
                question.tags(),
                question.content()
        );
    }
}
