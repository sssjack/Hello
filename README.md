# 面试真题 AI 训练系统

这是一个面向省考、国考、事业编结构化面试备考的全栈项目。系统可以按年份或考试类型从 MySQL 题库抽取面试题，支持浏览器语音输入和文字输入作答，并调用 DeepSeek 兼容 Chat Completions API 返回评分、内容建议、改进方向、解题思路、金句和参考提纲。

> 安全提示：不要把真实 DeepSeek API Key 写入代码或提交到 Git。请通过环境变量 `DEEPSEEK_API_KEY` 注入。

## 功能

- 随机抽取历年省考/国考/事业编面试题。
- 支持按年份、考试类型筛选。
- 支持 5 分钟倒计时训练，并记录本次作答用时。
- 支持文字作答。
- 支持浏览器 Web Speech API 语音转文字。
- Java 后端对接 DeepSeek API 进行严格结构化评分。
- 输出语言表达、内容深入、角度多元、政务思维及个性亮点、紧扣题意、逻辑结构六个维度的 10 分制评分和雷达图。
- MySQL 存储题库与评分记录。
- Redis 缓存题目年份等热点元数据。
- Flyway 自动初始化数据库表和基础题库。

## 技术栈

- 后端：Java 17、Spring Boot 3、Spring Web、Spring JDBC、Validation。
- 数据库：MySQL 8。
- 缓存：Redis 7。
- 数据迁移：Flyway。
- 前端：Spring Boot 静态资源托管的 HTML/CSS/JavaScript。
- AI：DeepSeek Chat Completions 兼容接口。

## 本地启动

### 1. 启动 MySQL 和 Redis

```bash
docker compose up -d mysql redis
```

### 2. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env，填写 DEEPSEEK_API_KEY
```

也可以直接在终端设置：

```bash
export DEEPSEEK_API_KEY="sk-your-deepseek-key"
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

访问：<http://localhost:8080>

### 4. 一键容器化启动

```bash
DEEPSEEK_API_KEY="sk-your-deepseek-key" docker compose up --build
```

## API 概览

### 随机抽题

```http
GET /api/questions/random?year=2024&type=NATIONAL
```

`type` 可选：

- `NATIONAL`：国考
- `PROVINCIAL`：省考
- `INSTITUTION`：事业编

### 获取年份

```http
GET /api/questions/years
```

### 提交评分

```http
POST /api/evaluations
Content-Type: application/json

{
  "questionId": 1,
  "answer": "我的回答内容……",
  "answerDurationSeconds": 168
}
```

## 添加更多真题

推荐向 `questions` 表追加数据，字段说明见 `sql/001_schema.sql`。也可以新增 Flyway 迁移脚本，例如：

```text
src/main/resources/db/migration/V3__more_questions.sql
```

请确保题目来源可核验，必要时在 `source` 字段中注明出处或整理批次。

## 文档

- [部署文档](docs/deployment.md)
- [设计文档](docs/design.md)
- [架构文档](docs/architecture.md)
- [SQL 文件](sql/README.md)
