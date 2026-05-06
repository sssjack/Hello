package com.interview.coach.dto;

import java.util.List;

public record EvaluationResult(
        Integer score,
        String level,
        List<String> strengths,
        List<String> weaknesses,
        String contentAdvice,
        String structureAdvice,
        String expressionAdvice,
        List<String> answerFramework,
        List<String> goldenSentences,
        List<String> sampleAnswerOutline
) {
    public static EvaluationResult fallback(String message) {
        return new EvaluationResult(
                0,
                "暂未评分",
                List.of(),
                List.of(message),
                "请检查 AI 服务配置后重试。",
                "可先按照“表态—分析—对策—升华”的结构整理答案。",
                "建议控制语速、减少口头禅，并使用短句表达。",
                List.of("明确问题本质", "分析原因和影响", "提出分层对策", "结合岗位价值收束"),
                List.of("民生无小事，枝叶总关情。", "把群众的急难愁盼作为工作的出发点和落脚点。"),
                List.of("开头回应题干", "主体分点论证", "结尾联系执行与服务")
        );
    }
}
