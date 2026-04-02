# Spring AI Alibaba Skill 支持 - 改造说明

## 📚 概述

本项目已完成对 Spring AI Alibaba 原生 Skill 机制的支持改造，可以通过 SKILL.md 文件定义和加载 Skills。

## 🎯 核心组件

### 1. **SkillLoader** (`config/SkillLoader.java`)
负责从 `skills` 目录加载 SKILL.md 文件并解析元数据。

**功能**:
- 扫描 skills 目录下所有子目录
- 解析 SKILL.md 文件的 YAML Front Matter
- 提取 skill 的 name、aliases、description
- 根据配置过滤启用的 skills

### 2. **SkillProperties** (`config/SkillProperties.java`)
Skills 配置属性类，从 application.yml 读取配置。

**配置项**:
```yaml
ai:
  skills:
    enabled: true                    # 是否启用 Skills
    path: src/main/java/com/aiwork/helper/skills  # Skills 目录
    enabled-skills:                  # 启用的 Skills 列表
      - amap-weather
```

### 3. **ToolCallbackConfig** (`config/ToolCallbackConfig.java`)
为所有 Tool 工具类注册 MethodToolCallbackProvider。

**已注册的 Tools**:
- TodoTools
- ApprovalTools
- KnowledgeTools
- TimeParserTool
- UserQueryTool
- ChatTools
- FileTools
- WeatherTools (新增)

### 4. **SkillRegistryConfig** (`config/SkillRegistryConfig.java`)
Skill 注册配置类，用于注册特定的 Skill Bean。

### 5. **WeatherTools** (`ai/tools/WeatherTools.java`)
高德天气查询 Tool 实现（基于 AMap SKILL.md）。

**提供的方法**:
- `queryWeather(city)` - 查询实时天气
- `queryWeatherForecast(city)` - 查询天气预报

## 📁 目录结构

```
AIWorkHelper-Java/
├── src/main/java/com/aiwork/helper/
│   ├── ai/
│   │   ├── agent/
│   │   │   └── AgentService.java          ← 集成 SkillLoader
│   │   └── tools/
│   │       └── WeatherTools.java          ← 新增天气查询 Tool
│   ├── config/
│   │   ├── AIConfig.java
│   │   ├── ToolCallbackConfig.java        ← 新增：Tool 回调配置
│   │   ├── SkillRegistryConfig.java       ← 新增：Skill 注册配置
│   │   ├── SkillProperties.java           ← 新增：Skill 配置类
│   │   └── SkillLoader.java               ← 新增：Skill 加载器
│   └── skills/
│       └── AMap/
│           └── SKILL.md                   ← Skill 定义文件
└── src/main/resources/
    └── application.yml                    ← 添加 Skills 配置
```

## 🚀 使用方式

### 方法 1: 创建新的 Skill

#### 步骤 1: 在 skills 目录下创建目录
```bash
src/main/java/com/aiwork/helper/skills/
└── YourSkill/
    └── SKILL.md
```

#### 步骤 2: 编写 SKILL.md 文件
```markdown
---
name: your-skill-name
aliases: [别名 1, 别名 2]
description: |
  Skill 的描述信息，说明这个 Skill 能做什么
---

# Skill 详细使用说明

## 使用方法
...

## API 参数
...

## 示例
...
```

#### 步骤 3: 创建对应的 Tool 实现类
```java
package com.aiwork.helper.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class YourSkillTools {
    
    @Tool(description = "工具方法的描述")
    public String yourMethod(
        @ToolParam(description = "参数描述", required = true) String param
    ) {
        // 实现逻辑
        return "结果";
    }
}
```

#### 步骤 4: 在 ToolCallbackConfig 中注册
```java
@Bean
public MethodToolCallbackProvider yourSkillToolsCallbacks(
    com.aiwork.helper.ai.tools.YourSkillTools yourSkillTools
) {
    log.info("注册 YourSkill Tool 回调");
    return MethodToolCallbackProvider.builder()
            .toolObjects(yourSkillTools)
            .build();
}
```

#### 步骤 5: 在 application.yml 中配置
```yaml
ai:
  skills:
    enabled-skills:
      - your-skill-name
```

### 方法 2: 使用已有的 Skill

项目已内置高德天气 Skill，直接使用即可：

**示例对话**:
```
用户：北京今天天气怎么样？
AI: 【北京市实时天气】
    天气：晴
    温度：25℃
    风向：东南风
    风力：2 级
    湿度：45%
    
    💡 温馨提示：气温适宜，适合户外活动。
```

## 🔧 AgentService 改造

AgentService 已集成 SkillLoader，启动时自动加载所有 Skills：

```java
@Service
public class AgentService {
    private final ChatClient chatClient;
    private final SkillLoader skillLoader;
    
    @PostConstruct
    public void init() {
        List<SkillInfo> skills = skillLoader.loadAllSkills();
        log.info("已加载 {} 个 Skills", skills.size());
    }
    
    public String chat(String userId, String userInput, ...) {
        // Skill 会自动被 ChatClient 识别和调用
        // 因为所有 Tool 都已通过 MethodToolCallbackProvider 注册
    }
}
```

## 📝 SKILL.md 文件格式

### YAML Front Matter
```yaml
---
name: skill-唯一名称
aliases: [别名 1, 别名 2, 中文别名]
description: |
  Skill 的描述，说明用途和能力
---
```

### 必填字段
- `name`: Skill 的唯一标识符
- `description`: Skill 的功能描述

### 可选字段
- `aliases`: 别名列表，用于意图识别

### 内容部分
Markdown 格式的详细说明，包括：
- 使用方法
- API 参数
- 示例代码
- 错误处理
- 最佳实践

## 🌟 技术亮点

### 1. **声明式 Skill 定义**
通过 SKILL.md 文件声明式地定义 Skill，无需硬编码。

### 2. **自动加载机制**
启动时自动扫描 skills 目录，加载所有启用的 Skill。

### 3. **统一 Tool 注册**
所有 Tool 通过 MethodToolCallbackProvider 统一注册到 ChatClient。

### 4. **灵活配置**
通过 application.yml 控制 Skills 的启用和禁用。

### 5. **易于扩展**
添加新 Skill 只需 3 步：
1. 创建 SKILL.md
2. 实现 Tool 类
3. 注册 Bean

## 🔍 调试技巧

### 查看 Skill 加载日志
```bash
# 启动日志中会显示
INFO  SkillLoader - 加载 Skill: amap-weather - Get real-time weather information
INFO  AgentService - AgentService 初始化，已加载 1 个 Skills
INFO  AgentService -   - Skill: amap-weather - Get real-time weather information
```

### 测试 Skill 调用
```bash
# 直接询问天气相关问题
curl -X POST http://localhost:8888/v1/chat/ai \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user-id",
    "message": "北京天气怎么样？",
    "relationId": "test-group-id"
  }'
```

## 📊 与 Go 版本的对应关系

| Go 版本 (skillcenter) | Java 版本 |
|----------------------|----------|
| skillcenter.LoadSkillsFromDir() | SkillLoader.loadAllSkills() |
| skill.ParseFrontMatter() | SkillLoader.parseSkillMd() |
| skill.RegisterToAgent() | MethodToolCallbackProvider |
| skills/*.md | skills/*/SKILL.md |

## ⚠️ 注意事项

1. **API Key 管理**: 高德天气 API Key 应从环境变量或配置文件读取，不要硬编码
2. **错误处理**: Tool 方法应该捕获异常并返回友好提示
3. **日志记录**: 关键步骤添加日志便于调试
4. **性能优化**: 大量 Skills 时考虑懒加载
5. **版本兼容**: 确保 Spring AI Alibaba 版本支持 MethodToolCallbackProvider

## 🎯 下一步计划

1. ✅ 基础 Skill 加载机制
2. ✅ 高德天气 Skill 实现
3. ⏳ 更多实用 Skills（地图搜索、新闻查询等）
4. ⏳ Skill 热加载（无需重启）
5. ⏳ Skill 市场（共享社区）

## 📖 参考资源

- Spring AI Alibaba 官方文档
- Spring AI Tool Annotations: https://docs.spring.io/spring-ai/reference/api/tools.html
- 高德天气 API: https://lbs.amap.com/api/webservice/guide/api/weatherinfo

---

**作者**: IT 杨秀才  
**公众号**: 后端，AI 知识进阶，后端面试场景题大全  
**项目**: AIWorkHelper-Java
