# 部署文档

## 1. 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8+
- Redis 7+
- Docker / Docker Compose（可选）

## 2. 配置项

| 变量 | 说明 | 默认值 |
| --- | --- | --- |
| `DEEPSEEK_API_KEY` | DeepSeek API Key，生产必填 | 空 |
| `DEEPSEEK_BASE_URL` | DeepSeek API 地址 | `https://api.deepseek.com` |
| `DEEPSEEK_MODEL` | 模型名称 | `deepseek-chat` |
| `MYSQL_URL` | MySQL JDBC 地址 | 本地 `interview_coach` |
| `MYSQL_USERNAME` | MySQL 用户名 | `interview` |
| `MYSQL_PASSWORD` | MySQL 密码 | `interview123` |
| `REDIS_HOST` | Redis 主机 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | 空 |
| `SERVER_PORT` | 应用端口 | `8080` |

请不要把真实 `DEEPSEEK_API_KEY` 写入仓库。用户提供的密钥应只放在服务器环境变量、CI/CD Secret 或 `.env` 本地文件中。

## 3. Docker Compose 部署

```bash
cp .env.example .env
# 编辑 .env，填入 DEEPSEEK_API_KEY
DEEPSEEK_API_KEY="sk-your-deepseek-key" docker compose up --build -d
```

访问：

```text
http://服务器IP:8080
```

查看日志：

```bash
docker compose logs -f app
```

停止服务：

```bash
docker compose down
```

## 4. 裸机部署

### 4.1 初始化 MySQL

应用默认启用 Flyway，首次启动会自动建表并导入基础题库。也可以手动执行：

```bash
mysql -u root -p < sql/000_create_database.sql
mysql -u root -p interview_coach < sql/001_schema.sql
mysql -u root -p interview_coach < sql/002_seed_questions.sql
```

### 4.2 启动 Redis

```bash
redis-server
```

### 4.3 构建应用

```bash
mvn clean package
```

### 4.4 运行应用

```bash
export DEEPSEEK_API_KEY="sk-your-deepseek-key"
export MYSQL_URL="jdbc:mysql://localhost:3306/interview_coach?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
export MYSQL_USERNAME="interview"
export MYSQL_PASSWORD="interview123"
export REDIS_HOST="localhost"
java -jar target/interview-coach-0.1.0.jar
```

## 5. 生产建议

1. 使用 Nginx 反向代理并开启 HTTPS。
2. MySQL 和 Redis 不要直接暴露公网。
3. API Key 使用 Secret 管理，不进入镜像和代码仓库。
4. 为 `evaluations` 表设置数据保留策略，避免长期积累过多文本数据。
5. 增加访问频率限制，控制模型调用成本。
