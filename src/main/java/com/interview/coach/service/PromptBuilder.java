package com.interview.coach.service;

import com.interview.coach.domain.Question;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String build(Question question, String answer, Integer answerDurationSeconds) {
        return """
                你是一名严格、专业、熟悉中国公务员/事业单位结构化面试的考官。
                请基于题目和考生原回答进行真实考场视角的评分与辅导，不要空泛鼓励。

                【题目信息】
                年份：%s
                类型：%s
                地区：%s
                来源：%s
                题目：%s

                【考生原回答】
                %s

                【前端记录作答用时】
                %s

                【输出要求】
                只输出合法 JSON，不要输出 Markdown，不要包裹代码块。字段名必须完整且完全一致：
                {
                  "score": 0,
                  "level": "较差/中等/中上/优秀/高分答案",
                  "questionType": "题型判断",
                  "questionTypeReason": "判断依据",
                  "answerDurationSeconds": null,
                  "durationComment": "对作答用时的考场评价",
                  "dimensionScores": [
                    {"name":"语言表达","score":0,"comment":"具体扣分或亮点"},
                    {"name":"内容深入","score":0,"comment":"具体扣分或亮点"},
                    {"name":"角度多元","score":0,"comment":"具体扣分或亮点"},
                    {"name":"政务思维及个性亮点","score":0,"comment":"具体扣分或亮点"},
                    {"name":"紧扣题意","score":0,"comment":"具体扣分或亮点"},
                    {"name":"逻辑结构","score":0,"comment":"具体扣分或亮点"}
                  ],
                  "scoreExplanation": "真实考场总评",
                  "scoreGapAssessment": "能否拉开分差",
                  "majorDeductions": ["主要扣分点"],
                  "examinerHighlights": ["考官能听出的亮点"],
                  "strengths": ["可保留和强化的亮点"],
                  "weaknesses": ["具体不足"],
                  "examinerPerspective": ["从审题、立意、逻辑、内容、措施、表达等角度评价"],
                  "sentenceLevelProblems": ["逐句或逐段诊断"],
                  "priorityImprovements": ["按优先级排序的修改建议"],
                  "contentAdvice": "内容层面建议",
                  "structureAdvice": "结构层面建议",
                  "expressionAdvice": "表达层面建议",
                  "answerFramework": ["本题解题思路"],
                  "goldenSentences": ["适合本题的规范表达或金句"],
                  "optimizedAnswer": ["保留考生风格后的高分改写"],
                  "sampleAnswer": ["高分示例回答"],
                  "memorizationOutline": ["开头一句话", "第一层", "第二层", "第三层", "结尾一句话"],
                  "deliveryAdvice": ["考场表达建议"],
                  "transferableScenarios": ["可迁移的相似题型"],
                  "sampleAnswerOutline": ["参考提纲"]
                }

                评价必须结合考生原回答中的具体表达；如果原回答过短、过空或缺少段落，也要明确指出会如何扣分。
                """.formatted(
                question.examYear() == null ? "未指定" : question.examYear(),
                question.examType() == null ? "未指定" : question.examType().getLabel(),
                blankToDefault(question.province(), "全国/未注明"),
                blankToDefault(question.source(), "题库收录"),
                question.content(),
                answer,
                answerDurationSeconds == null ? "未记录" : answerDurationSeconds + " 秒"
        );
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
