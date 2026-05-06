package com.interview.coach.service;

import com.interview.coach.domain.Question;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String build(Question question, String answer, Integer answerDurationSeconds) {
        return """
                你是一名长期参与省考、事业编、国考结构化面试评分的专业面试官，同时也是面试培训教研老师。请站在真实考场评分视角，对考生作答进行严格、专业、可操作的评价。不要安慰式评价，不要泛泛而谈，要给出真正能提分的面试官视角反馈。

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

                【必须完成的评价任务】
                一、先判断题型：判断题型，例如综合分析题、组织管理题、应急应变题、人际关系题、自我认知题、情景模拟题、岗位匹配题、漫画寓言题等，并说明判断依据。
                二、给出真实考场评分：100分制，说明当前水平、在省考/事业编/国考中能否拉开分差、主要扣分点、考官能听出来的亮点。
                三、从面试官视角逐项评价：审题、立意、逻辑、内容、措施、表达、岗位匹配、模板痕迹、金句/政策/机关话术九个角度都要覆盖。
                四、指出具体问题：逐句或逐段指出哪句话可删、哪句话要改、哪里应补充、哪里不够机关化、哪里显得幼稚/空泛/情绪化/不成熟。
                五、提炼亮点：思路、观点、措施、表达、能打动考官的地方。
                六、给出改进方向：按优先级排序，包括最应该先改、第二应该改、最能快速提分、长期重点提升能力。
                七、优化成高分答案：保留原有观点和表达风格，改写成2-3分钟考场表达，结构清楚、自然、有高度、有具体做法。
                八、另给一版示例回答：从高分考生角度重新组织，不是简单润色；自然入题，有层次，有机关场景感，体现政府思维、群众立场、责任意识、法治意识、服务意识，2-3分钟。
                九、给出背诵提纲：格式必须包含“开头一句话、第一层、第二层、第三层、结尾一句话”。
                十、给出考场表达建议：语速节奏、停顿位置、强调词、避免表达、如何避免背稿感。
                十一、给出横向拓展：说明可迁移到哪些类似题，给出3个相似题型切入点。

                【六个维度打分，每项10分】
                1. 语言表达：口语自然度、规范度、考场感、流畅度。
                2. 内容深入：分析是否深入，是否有原因、影响、本质和可执行细节。
                3. 角度多元：是否能从群众、政府、制度、执行、协同等多角度展开。
                4. 政务思维及个性亮点：是否体现政治素养、群众立场、服务意识、法治意识、岗位匹配和个人辨识度。
                5. 紧扣题意：是否抓住身份、场景、矛盾、任务要求，有无跑偏漏点。
                6. 逻辑结构：层次、衔接、分点、总分总、时间/主体/流程逻辑。

                【输出要求】
                只输出合法 JSON，不要输出 Markdown，不要包裹代码块。字段必须完整，字段名必须完全一致：
                {
                  "score": 0到100的整数,
                  "level": "较差/中等/中上/优秀/高分答案",
                  "questionType": "题型判断",
                  "questionTypeReason": "判断依据",
                  "answerDurationSeconds": 作答用时秒数，若无记录填null,
                  "durationComment": "对作答用时的考场评价",
                  "dimensionScores": [
                    {"name":"语言表达","score":0到10整数,"comment":"具体扣分或亮点"},
                    {"name":"内容深入","score":0到10整数,"comment":"具体扣分或亮点"},
                    {"name":"角度多元","score":0到10整数,"comment":"具体扣分或亮点"},
                    {"name":"政务思维及个性亮点","score":0到10整数,"comment":"具体扣分或亮点"},
                    {"name":"紧扣题意","score":0到10整数,"comment":"具体扣分或亮点"},
                    {"name":"逻辑结构","score":0到10整数,"comment":"具体扣分或亮点"}
                  ],
                  "scoreExplanation":"真实考场总评：水平、能否拉开分差、总体印象",
                  "scoreGapAssessment":"省考/事业编/国考场景下是否能拉开分差",
                  "majorDeductions":["主要扣分点，3-6条"],
                  "examinerHighlights":["考官能听出来的亮点，2-5条"],
                  "strengths":["可保留和强化的亮点，3-6条"],
                  "weaknesses":["具体不足，3-6条"],
                  "examinerPerspective":["按审题/立意/逻辑/内容/措施/表达/岗位匹配/模板痕迹/金句话术逐项评价，至少9条"],
                  "sentenceLevelProblems":["逐句或逐段诊断，指出可删/要改/应补/不机关化/显幼稚空泛的位置，至少5条；如原文信息不足也要指出风险"],
                  "priorityImprovements":["按优先级排序的修改建议，4-6条"],
                  "contentAdvice":"内容层面改进建议",
                  "structureAdvice":"结构层面改进建议",
                  "expressionAdvice":"表达层面改进建议",
                  "answerFramework":["本题解题思路，4-6条"],
                  "goldenSentences":["适合本题但不过度堆砌的金句/机关话术/政策表达，3-6条"],
                  "optimizedAnswer":["保留考生原有观点与风格后的高分改写，分段输出，2-3分钟"],
                  "sampleAnswer":["高分考生标准示范答案，分段输出，2-3分钟"],
                  "memorizationOutline":["开头一句话：...","第一层：...","第二层：...","第三层：...","结尾一句话：..."],
                  "deliveryAdvice":["语速节奏/停顿/强调/避免表达/避免背稿感，5-8条"],
                  "transferableScenarios":["相似题型1+切入点","相似题型2+切入点","相似题型3+切入点"],
                  "sampleAnswerOutline":["参考提纲，4-7条"]
                }

                评价必须结合考生原回答中的具体表述；如果原回答太短、太空或缺少段落，也要明确指出这会怎样扣分。不要编造不存在的题目背景。
                """.formatted(
                question.examYear(),
                question.examType().getLabel(),
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
