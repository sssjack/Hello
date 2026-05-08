package com.interview.coach.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EvaluationRequest(
        Long questionId,
        @Size(max = 3000, message = "自定义题目不能超过 3000 字")
        String customQuestion,
        @NotBlank(message = "回答内容不能为空")
        @Size(min = 20, max = 6000, message = "回答内容建议在 20 到 6000 字之间")
        String answer,
        @Min(value = 0, message = "作答用时不能为负数")
        @Max(value = 3600, message = "作答用时不能超过 3600 秒")
        Integer answerDurationSeconds
) {
}
