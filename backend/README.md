# Backend · AIGC 个性化学习平台

Spring Boot 后端服务，为 AIGC 个性化学习平台提供 REST API。

---

## 技术栈

- **Spring Boot** 4.0.1 · **Java** 17
- **Spring Data JPA** + MySQL 8
- **Spring Security** + JWT
- **Spring Data Redis**
- **Spring Mail**（邮箱验证码）
- **通义千问** OpenAI 兼容 API

---

## 目录结构

```
backend/
├── pom.xml
├── mvnw / mvnw.cmd
├── src/
│   ├── main/
│   │   ├── java/com/example/learningplatform/
│   │   │   ├── config/          # 安全、CORS、Redis、AI 配置
│   │   │   ├── controller/      # REST 控制器
│   │   │   ├── dto/             # 请求 / 响应对象
│   │   │   ├── entity/          # JPA 实体
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── security/        # JWT 过滤器与用户详情
│   │   │   ├── service/         # 业务逻辑
│   │   │   └── util/            # 工具类
│   │   └── resources/
│   │       ├── application.properties.example
│   │       └── application.properties   # 本地配置，不提交 Git
│   └── test/
└── .gitignore
```

---

## 快速启动

### 1. 准备环境

| 依赖 | 版本建议 |
|------|----------|
| JDK | 17+ |
| Maven | 3.8+（或使用项目自带 `mvnw`） |
| MySQL | 8.0+ |
| Redis | 6.0+ |

### 2. 配置应用

```bash
# Windows
copy src\main\resources\application.properties.example src\main\resources\application.properties

# Linux / macOS
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

编辑 `application.properties`，填写以下配置：

| 配置项 | 说明 |
|--------|------|
| `spring.datasource.*` | MySQL 数据库连接 |
| `spring.mail.*` | QQ 邮箱 SMTP 与授权码 |
| `ai.api-key` | 阿里云通义千问 API Key |
| `spring.data.redis.*` | Redis 连接信息 |

### 3. 初始化数据库

```sql
CREATE DATABASE aigc_learning_platform
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

> JPA 配置了 `ddl-auto=update`，首次启动会自动建表。

### 4. 运行

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

服务默认监听 **http://localhost:9090**。

### 5. 验证

```bash
curl http://localhost:9090/api/auth/health
```

（如有健康检查端点；也可直接调用登录 / 注册接口测试。）

---

## 主要 API 模块

| 路径前缀 | 控制器 | 功能 |
|----------|--------|------|
| `/api/auth` | `AuthController` | 认证与账号 |
| `/api/profile` | `LearningProfileController` | 学习画像 |
| `/api/user-subject` | `UserSubjectController` | 用户学科 |
| `/api/admin/subjects` | `SubjectController` | 学科管理 |
| `/api/admin/knowledge-points` | `KnowledgePointController` | 知识点 |
| `/api/admin/resources` | `ParseResourceController` | 资源解析入库 |
| `/api/admin/assessment-questions` | `SubjectAssessmentQuestionController` | 测评题 |
| `/api/ai/test` | `AiTestController` | AI 调试 |

---

## 构建与打包

```bash
./mvnw clean package -DskipTests
java -jar target/learningplatform-0.0.1-SNAPSHOT.jar
```

---

## 注意事项

- `application.properties` 含敏感信息，已通过 `.gitignore` 排除，**请勿提交到 Git**
- 仅提交 `application.properties.example` 作为配置模板
- 与前端联调时，确保 CORS 已放行前端开发地址（见 `CorsConfig`）

---

## 许可证

[Apache License 2.0](../LICENSE)
