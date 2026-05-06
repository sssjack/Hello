# 架构文档

## 1. 总体架构

```text
浏览器
  │
  │  HTML/CSS/JS + Web Speech API + 5分钟倒计时 + 雷达图
  ▼
Spring Boot 应用
  ├─ QuestionController：年份查询、随机抽题
  ├─ EvaluationController：提交作答、作答用时并评分
  ├─ QuestionService：题目筛选、缓存读取
  ├─ EvaluationService：评分编排、评分记录保存
  ├─ DeepSeekClient：调用 DeepSeek Chat Completions API
  ├─ MySQL：题库、评分记录
  └─ Redis：年份等热点元数据缓存
```

## 2. 模块说明

### 前端静态页面

路径：`src/main/resources/static`

- `index.html`：页面结构。
- `styles.css`：响应式样式。
- `app.js`：抽题、5分钟倒计时、作答用时统计、语音输入、评分提交、六维雷达图和结果渲染。

### Java 后端

路径：`src/main/java/com/interview/coach`

- `controller`：REST API。
- `service`：业务逻辑和 DeepSeek 对接。
- `repository`：JDBC 数据访问。
- `domain`：领域模型。
- `dto`：接口请求与响应对象。
- `config`：DeepSeek 配置、Redis 缓存配置。

### 数据层

- MySQL 保存结构化题库和评分记录。
- Redis 通过 Spring Cache 保存短时热点数据。
- Flyway 管理数据库版本。

## 3. 请求链路

### 随机抽题链路

```text
GET /api/questions/random
  -> QuestionController
  -> QuestionService.randomQuestion
  -> QuestionRepository.findByFilters
  -> MySQL
  -> 返回 QuestionResponse
```

### AI 评分链路

```text
POST /api/evaluations
  -> EvaluationController
  -> 校验 answer + answerDurationSeconds
  -> EvaluationService.evaluate
  -> QuestionService.getQuestion
  -> PromptBuilder.build
  -> DeepSeekClient.evaluate
  -> EvaluationRepository.save
  -> 返回 EvaluationResult
```

## 4. 缓存策略

当前 Redis 缓存 TTL 为 5 分钟：

- `questionYears`：缓存可选年份列表，降低题库元数据查询压力。

随机抽题接口默认不缓存最终题目，保证用户连续点击时更符合“随机训练”的直觉。若未来题库规模较大，可改为缓存候选题 ID 列表，再在应用层随机取题。

## 5. 可扩展方向

1. 用户登录与个人训练历史。
2. 按题型标签筛选，例如综合分析、组织管理、应急应变、人际沟通。
3. 完整语音评分：接入服务端 ASR，并分析语速、停顿和口头禅。
4. 管理后台：题库导入、题目审核、来源管理。
5. 将六维评分趋势沉淀为个人能力画像。
5. 分数趋势图和弱项分析。
