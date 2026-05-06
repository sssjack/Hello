package com.interview.coach.domain;

import java.time.LocalDateTime;

public record Question(
        Long id,
        Integer examYear,
        ExamType examType,
        String province,
        String source,
        String tags,
        String content,
        LocalDateTime createdAt
) {
}
