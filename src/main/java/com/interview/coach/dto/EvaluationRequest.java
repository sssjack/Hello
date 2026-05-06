package com.interview.coach.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EvaluationRequest(
        @NotNull(message = "questionId 不能为空") Long questionId,
        @NotBlank(message = "回答内容不能为空")
        @Size(min = 20, max = 6000, message = "回答内容建议在 20 到 6000 字之间")
        String answer
) {
}
