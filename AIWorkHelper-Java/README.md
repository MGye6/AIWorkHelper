# AIWorkHelper-Java

AI智能办公助手系统 - Java版本 (基于 Spring Boot 3.x + Spring AI Alibaba)

## 项目简介

这是Go版本AIWorkHelper的Java重写版本，使用Spring Boot 3.x框架和Spring AI Alibaba进行开发，提供完全一致的功能。项目采用了现代化的AI Agent架构，基于Spring AI的Function Calling机制实现智能意图识别和工具调用。

## 技术栈

- **Java**: JDK 21
- **框架**: Spring Boot 3.2.0
- **数据库**: MongoDB 5.0+
- **向量存储**: Redis 6.0+ (需要 RediSearch 模块)
- **认证**: JWT (JSON Web Token)
- **AI集成**: Spring AI Alibaba (阿里云通义千问 DashScope)
  - AI Agent: 基于 Function Calling 机制
  - 会话记忆: SummaryBufferChatMemory（摘要缓冲记忆）
  - 知识库: RAG（检索增强生成）
- **WebSocket**: Spring WebSocket
- **构建工具**: Maven 3.6+
- **PDF处理**: Apache PDFBox

## 核心功能模块

### 0. 🆕 Skill 扩展机制（新增）
- **声明式 Skill 定义**: 通过 SKILL.md 文件定义新能力
- **自动加载机制**: 启动时扫描并加载所有 Skills
- **统一 Tool 注册**: MethodToolCallbackProvider 统一管理
- **灵活配置**: application.yml 控制启用/禁用
- **内置 Skills**:
  - 高德天气查询（amap-weather）
  - 更多 Skills 等你来创建...

### 1. 用户管理
- 用户注册、登录
- 用户CRUD操作
- 密码修改
- JWT认证和授权

### 2. 待办事项管理
- 创建、查询、编辑、删除待办
- 待办完成状态管理
- 多执行人分配
- 操作记录跟踪

### 3. 审批流程管理
- 支持多种审批类型（请假、补卡、外出等）
- 多级审批流程
- 审批状态追踪
- 抄送功能

### 4. 部门组织管理
- 部门树形结构管理
- 部门负责人设置
- 部门用户关联

### 5. WebSocket实时聊天
- 群聊功能（支持多群聊）
- 私聊功能
- 连接管理（双向映射表）
- 消息广播和单播
- 单设备登录策略

### 6. AI智能助手（Agent架构）

基于Spring AI的Function Calling机制实现，LLM自动分析用户意图并选择合适的Tool执行：

| Tool工具类 | 功能描述 |
|-----------|---------|
| TodoTools | 创建待办、查询待办列表 |
| ApprovalTools | 创建请假/补卡/外出审批、查询审批记录 |
| KnowledgeTools | 知识库RAG查询、更新知识库、清空知识库 |
| TimeParserTool | 自然语言时间解析为Unix时间戳 |
| UserQueryTool | 根据用户名查询用户ID |
| ChatTools | 获取聊天记录、总结群聊消息 |
| FileTools | 获取用户上传的文件列表 |
| WeatherTools | 高德天气查询（新增 Skill） |

**Skill 扩展**: 可以通过 SKILL.md 文件轻松扩展新的能力，详见 [docs/SKILL_QUICKSTART.md](docs/SKILL_QUICKSTART.md)

**会话记忆机制**：使用 `SummaryBufferChatMemory`，当对话Token超过限制时，自动调用LLM生成摘要，保留关键信息的同时控制Token消耗。

### 7. 知识库（RAG）
- PDF文档解析和向量化
- 基于Redis + RediSearch的向量存储
- 语义检索增强生成

### 8. 文件上传
- 文件上传功能
- PDF文档解析（知识库）

## 项目结构

```
AIWorkHelper-Java/
├── src/
│   ├── main/
│   │   ├── java/com/aiwork/helper/
│   │   │   ├── AIWorkHelperApplication.java    # 主应用类
│   │   │   ├── config/                          # 配置类
│   │   │   │   ├── JwtProperties.java           # JWT配置
│   │   │   │   ├── WebSocketConfig.java         # WebSocket配置
│   │   │   │   ├── WebSocketServerConfig.java   # WebSocket服务器配置
│   │   │   │   └── AIConfig.java                # AI配置
│   │   │   ├── controller/                      # 控制器层
│   │   │   │   ├── UserController.java
│   │   │   │   ├── TodoController.java
│   │   │   │   ├── ApprovalController.java
│   │   │   │   ├── DepartmentController.java
│   │   │   │   ├── GroupController.java         # 群组管理
│   │   │   │   └── KnowledgeDiagController.java # 知识库诊断
│   │   │   ├── service/                         # 业务逻辑层
│   │   │   │   ├── UserService.java
│   │   │   │   ├── TodoService.java
│   │   │   │   ├── ApprovalService.java
│   │   │   │   ├── DepartmentService.java
│   │   │   │   ├── GroupService.java
│   │   │   │   └── AIService.java
│   │   │   ├── repository/                      # 数据访问层
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── TodoRepository.java
│   │   │   │   ├── ApprovalRepository.java
│   │   │   │   ├── DepartmentRepository.java
│   │   │   │   ├── ChatLogRepository.java
│   │   │   │   └── GroupMemberRepository.java
│   │   │   ├── entity/                          # 实体类
│   │   │   │   ├── User.java
│   │   │   │   ├── Todo.java
│   │   │   │   ├── Approval.java
│   │   │   │   ├── Department.java
│   │   │   │   ├── ChatLog.java
│   │   │   │   ├── GroupMember.java
│   │   │   │   ├── enums/                       # 枚举类
│   │   │   │   └── embedded/                    # 嵌入文档
│   │   │   ├── ai/                              # AI 相关（Agent 架构）
│   │   │   │   ├── agent/                       # Agent 服务
│   │   │   │   │   └── AgentService.java        # 核心 Agent（集成 Skills）
│   │   │   │   ├── tools/                       # Tool 工具类
│   │   │   │   │   ├── TodoTools.java
│   │   │   │   │   ├── ApprovalTools.java
│   │   │   │   │   ├── KnowledgeTools.java
│   │   │   │   │   ├── TimeParserTool.java
│   │   │   │   │   ├── UserQueryTool.java
│   │   │   │   │   ├── ChatTools.java
│   │   │   │   │   ├── FileTools.java
│   │   │   │   │   └── WeatherTools.java        # 新增：高德天气 Skill
│   │   │   │   ├── memory/                      # 会话记忆
│   │   │   │   │   ├── SummaryBufferChatMemory.java
│   │   │   │   │   └── MultiSessionChatMemory.java
│   │   │   │   ├── config/                      # AI 配置
│   │   │   │   │   ├── ChatClientConfig.java
│   │   │   │   │   ├── ChatMemoryConfig.java
│   │   │   │   │   ├── ToolCallbackConfig.java  # 新增：Tool 回调注册
│   │   │   │   │   ├── SkillRegistryConfig.java # 新增：Skill 注册
│   │   │   │   │   ├── SkillProperties.java     # 新增：Skill 配置
│   │   │   │   │   └── SkillLoader.java         # 新增：Skill 加载器
│   │   │   │   └── knowledge/                   # 知识库
│   │   │   │       ├── PDFProcessor.java
│   │   │   │       └── VectorStoreService.java
│   │   │   ├── skills/                          # 新增：Skills 目录
│   │   │   │   └── AMap/
│   │   │   │       └── SKILL.md                 # Skill 定义文件
│   │   │   ├── websocket/                       # WebSocket处理器
│   │   │   │   ├── ChatWebSocketHandler.java
│   │   │   │   └── WebSocketHandshakeInterceptor.java
│   │   │   ├── security/                        # 安全相关
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── SecurityUtils.java
│   │   │   ├── dto/                             # 数据传输对象
│   │   │   │   ├── request/
│   │   │   │   ├── response/
│   │   │   │   └── websocket/
│   │   │   └── exception/                       # 异常处理
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       └── BusinessException.java
│   │   └── resources/
│   │       ├── application.yml                   # 配置文件
│   │       └── logback-spring.xml               # 日志配置
│   └── test/                                     # 测试代码
├── pom.xml                                       # Maven配置文件
├── logs/                                         # 日志文件目录
├── upload/                                       # 文件上传目录
└── docs/                                         # 文档目录
```

## 配置说明

### application.yml 主要配置

```yaml
spring:
  application:
    name: aiworkhelper

  # MongoDB 数据库配置
  data:
    mongodb:
      host: 127.0.0.1
      port: 27017
      database: aiworkhelper

    # Redis 配置（向量存储 + 缓存）
    redis:
      host: 127.0.0.1
      port: 6379
      database: 0

  # Spring AI DashScope 配置
  ai:
    dashscope:
      api-key: your-api-key-here
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      chat:
        options:
          model: qwen-max
          temperature: 0.7
      embedding:
        options:
          model: text-embedding-v3

# 服务器配置
server:
  port: 8888

# WebSocket 配置
websocket:
  server:
    port: 9000
    path: /ws

# JWT Token 配置
jwt:
  secret: kuangbiao.camps.aiworkhelper.jwt.secret.key.2024.secure.minimum.256bits
  expire: 8640000  # 100天

# AI Agent 记忆配置
ai:
  memory:
    max-token-limit: 2000  # Token超限时自动生成摘要
```

## 运行说明

### 前置条件

1. Java 21
2. Maven 3.6+
3. MongoDB 5.0+
4. Redis 6.0+ (推荐使用 Redis Stack，已内置 RediSearch 模块)

### 启动步骤

1. 启动 MongoDB 和 Redis 服务
   ```bash
   # 推荐使用 Docker 启动 Redis Stack
   docker run -d --name redis-stack -p 6379:6379 redis/redis-stack:latest
   ```

2. 配置 `application.yml` 中的 DashScope API Key

3. 运行主类
   ```bash
   mvn spring-boot:run
   ```

### 访问地址

- HTTP API: http://localhost:8888
- WebSocket: ws://localhost:9000/ws

## 与Go版本的对应关系

| Go版本 | Java版本 |
|--------|----------|
| main.go | AIWorkHelperApplication.java |
| internal/model/ | entity/ |
| internal/svc/ | config/ + service/ |
| internal/logic/ | service/ |
| internal/handler/ | controller/ + websocket/ |
| pkg/langchain/router/ | ai/agent/AgentService.java (Function Calling) |
| pkg/langchain/memoryx/ | ai/memory/SummaryBufferChatMemory.java |
| internal/logic/chatinternal/ | ai/tools/ (@Tool注解) |

## API文档

### 用户相关
- `POST /v1/user/login` - 用户登录
- `POST /v1/user/create` - 创建用户
- `PUT /v1/user/password` - 修改密码

### 待办相关
- `POST /v1/todo/create` - 创建待办
- `POST /v1/todo/list` - 查询待办列表
- `POST /v1/todo/finish` - 完成待办

### 审批相关
- `POST /v1/approval/create` - 创建审批
- `POST /v1/approval/list` - 查询审批列表
- `POST /v1/approval/dispose` - 处理审批

### 群组相关
- `POST /v1/group/create` - 创建群聊
- `POST /v1/group/members/add` - 添加群成员
- `GET /v1/group/{groupId}/members` - 获取群成员

### AI聊天
- `POST /v1/chat/ai` - AI聊天接口

## 许可证

MIT License
