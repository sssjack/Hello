package com.interview.coach.dto;

import java.util.List;

public record EvaluationResult(
        Integer score,
        String level,
        String questionType,
        String questionTypeReason,
        Integer answerDurationSeconds,
        String durationComment,
        List<DimensionScore> dimensionScores,
        String scoreExplanation,
        String scoreGapAssessment,
        List<String> majorDeductions,
        List<String> examinerHighlights,
        List<String> strengths,
        List<String> weaknesses,
        List<String> examinerPerspective,
        List<String> sentenceLevelProblems,
        List<String> priorityImprovements,
        String contentAdvice,
        String structureAdvice,
        String expressionAdvice,
        List<String> answerFramework,
        List<String> goldenSentences,
        List<String> optimizedAnswer,
        List<String> sampleAnswer,
        List<String> memorizationOutline,
        List<String> deliveryAdvice,
        List<String> transferableScenarios,
        List<String> sampleAnswerOutline
) {
    public record DimensionScore(String name, Integer score, String comment) {
    }

    public static EvaluationResult fallback(String message) {
        List<DimensionScore> dimensions = List.of(
                new DimensionScore("语言表达", 0, "未完成模型评分"),
                new DimensionScore("内容深入", 0, "未完成模型评分"),
                new DimensionScore("角度多元", 0, "未完成模型评分"),
                new DimensionScore("政务思维及个性亮点", 0, "未完成模型评分"),
                new DimensionScore("紧扣题意", 0, "未完成模型评分"),
                new DimensionScore("逻辑结构", 0, "未完成模型评分")
        );
        return new EvaluationResult(
                0,
                "暂未评分",
                "暂未判断",
                message,
                null,
                "未记录有效作答用时。",
                dimensions,
                "请检查 AI 服务配置后重试。",
                "暂无法判断是否能拉开分差。",
                List.of(message),
                List.of(),
                List.of(),
                List.of(message),
                List.of("审题、立意、结构、内容、措施、表达等维度需要在模型恢复后重新评估。"),
                List.of("当前无法进行逐句诊断，请先确保 DeepSeek API 配置可用。"),
                List.of("先恢复 AI 评分服务", "再围绕题干关键词完成一次完整作答", "最后根据维度分定位短板"),
                "请检查 AI 服务配置后重试。",
                "可先按照“表态—分析—对策—升华”的结构整理答案。",
                "建议控制语速、减少口头禅，并使用短句表达。",
                List.of("明确问题本质", "分析原因和影响", "提出分层对策", "结合岗位价值收束"),
                List.of("民生无小事，枝叶总关情。", "把群众的急难愁盼作为工作的出发点和落脚点。"),
                List.of("暂无优化答案。"),
                List.of("暂无示例回答。"),
                List.of("开头一句话：回应题干", "第一层：点明问题", "第二层：分析原因", "第三层：提出对策", "结尾一句话：联系岗位收束"),
                List.of("语速保持稳定", "关键分论点前稍作停顿", "避免机械背稿"),
                List.of("可迁移到基层治理类题目", "可迁移到窗口服务类题目", "可迁移到组织协调类题目"),
                List.of("开头回应题干", "主体分点论证", "结尾联系执行与服务")
        );
    }
}
