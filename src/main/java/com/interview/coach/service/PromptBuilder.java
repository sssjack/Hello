package com.interview.coach.service;

import com.interview.coach.domain.Question;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String build(Question question, String answer) {
        return """
                你是一名严格、专业、熟悉中国公务员/事业单位结构化面试的考官。请基于题目和考生原回答进行评分与辅导。

                【题目信息】
                年份：%s
                类型：%s
                地区：%s
                来源：%s
                题目：%s

                【考生原回答】
                %s

                【评分标准，总分100】
                1. 审题准确度：20分，判断是否抓住题干核心矛盾、身份定位、任务要求。
                2. 政策理解与价值立场：20分，判断是否体现服务群众、依法行政、实事求是等公共价值。
                3. 分析深度与逻辑结构：20分，判断是否有清晰层次、原因/影响/主体分析是否充分。
                4. 对策可行性：20分，判断措施是否具体、可执行、符合基层治理实际。
                5. 表达流畅度与面试风格：20分，判断语言是否自然、规范、有现场感。

                【输出要求】
                只输出合法 JSON，不要输出 Markdown，不要包裹代码块。字段必须完整：
                {
                  "score": 0到100的整数,
                  "level": "优秀/良好/一般/需提升",
                  "strengths": ["结合原回答的具体优点，2-4条"],
                  "weaknesses": ["结合原回答的具体不足，2-5条"],
                  "contentAdvice": "内容层面的改进建议，需结合题目和原回答",
                  "structureAdvice": "结构层面的改进建议，给出可复用答题框架",
                  "expressionAdvice": "表达层面的改进建议，包括语言、语速、衔接、金句使用边界",
                  "answerFramework": ["本题可采用的解题步骤，4-6条"],
                  "goldenSentences": ["适合本题的规范表达或金句，3-6条"],
                  "sampleAnswerOutline": ["参考提纲，不写完整背诵稿，4-7条"]
                }

                不能编造不存在的题目背景；不要空泛鼓励；必须指出下一次如何提升分数。
                """.formatted(
                question.examYear(),
                question.examType().getLabel(),
                blankToDefault(question.province(), "全国/未注明"),
                blankToDefault(question.source(), "题库收录"),
                question.content(),
                answer
        );
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
