# SQL 文件说明

- `000_create_database.sql`：创建并选择 `interview_coach` 数据库。
- `001_schema.sql`：数据库表结构，包含题库表 `questions` 和评分记录表 `evaluations`。
- `002_seed_questions.sql`：初始题库数据，可按相同字段继续追加经核验的历年省考/国考/事业编面试真题。
- `003_add_answer_duration.sql`：为评分记录增加 `answer_duration_seconds` 字段，用于保存前端记录的作答用时。

Spring Boot 启动时会通过 Flyway 自动执行 `src/main/resources/db/migration` 下的同名迁移脚本；如果需要手动初始化 MySQL，也可以按编号顺序执行本目录 SQL。
