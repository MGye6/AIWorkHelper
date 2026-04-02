# Skill 快速开始指南

## 🚀 5 分钟上手 Skill 功能

### 第 1 步：确认配置

检查 `application.yml` 中 Skills 配置是否启用：

```yaml
ai:
  skills:
    enabled: true
    path: src/main/java/com/aiwork/helper/skills
    enabled-skills:
      - amap-weather
```

### 第 2步：启动项目

```bash
mvn spring-boot:run
```

观察启动日志，应该看到：

```
INFO  SkillLoader - SkillLoader 初始化完成，Skills 路径：src/main/java/com/aiwork/helper/skills
INFO  SkillLoader - 加载 Skill: amap-weather (Get real-time weather information)
INFO  AgentService - AgentService 初始化，已加载 1 个 Skills
INFO  AgentService -   - Skill: amap-weather - Get real-time weather information
```

### 第 3 步：测试天气查询

通过 AI 聊天接口测试：

**请求**:
```bash
curl -X POST http://localhost:8888/v1/chat/ai \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user",
    "message": "北京今天天气怎么样？",
    "relationId": "group-001"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "data": "【北京市实时天气】\n天气：晴\n温度：25℃\n风向：东南风\n风力：2 级\n湿度：45%\n\n💡 温馨提示：气温适宜，适合户外活动。",
  "message": "success"
}
```

## 🎯 创建你的第一个 Skill

### 示例：创建笑话查询 Skill

#### 1. 创建目录结构

```bash
mkdir -p src/main/java/com/aiwork/helper/skills/Joke
```

#### 2. 编写 SKILL.md

创建文件 `src/main/java/com/aiwork/helper/skills/Joke/SKILL.md`:

```markdown
---
name: joke-teller
aliases: [讲笑话，笑话，joke]
description: |
  Tell jokes to make users happy. Use when users ask for jokes, 
  want to be entertained, or need mood lifting.
---

# 笑话大全 Skill

## 使用方法

当用户需要听笑话、想要娱乐或调节气氛时使用。

## 能力

- 随机笑话生成
- 支持多种类型（冷笑话、程序员笑话等）
- 根据上下文智能推荐

## 示例

**直接请求:**
```
给我讲个笑话
```

**场景化:**
```
工作好累，想听个笑话放松一下
```

**指定类型:**
```
讲个程序员相关的笑话
```
```

#### 3. 实现 Tool 类

创建 `src/main/java/com/aiwork/helper/ai/tools/JokeTools.java`:

```java
/**
 * @author: IT 杨秀才
 */
package com.aiwork.helper.ai.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class JokeTools {

    private static final List<String> JOKES = List.of(
        "程序员 A：'你结婚吗？' 程序员 B：'不结，我还没找到对象。' 程序员 A：'我也没找到。'",
        "为什么程序员总是分不清万圣节和圣诞节？因为 Oct 31 == Dec 25",
        "有个程序员去相亲，姑娘问他：'你有房吗？' 程序员答：'有啊，我有 Linux、Windows、Mac...'",
        "程序员的征婚启事：'本人擅长 CRUD，现诚征女友一名，要求会写 Hello World，非诚勿扰！'"
    );

    private final Random random = new Random();

    @Tool(description = "讲一个笑话给用户，用于娱乐或调节气氛。可以随机返回一个笑话。")
    public String tellJoke(
        @ToolParam(description = "笑话类型，可选（如'程序员'、'冷笑话'），目前暂不支持类型筛选", required = false) String type
    ) {
        log.info("Tool 调用 - tellJoke: type={}", type);
        
        try {
            // 随机选择一个笑话
            int index = random.nextInt(JOKES.size());
            String joke = JOKES.get(index);
            
            log.info("笑话查询成功");
            return "😄 来听个笑话：\n\n" + joke + "\n\n希望这个笑话能让你开心！";
            
        } catch (Exception e) {
            log.error("讲笑话失败", e);
            return "抱歉，笑话仓库暂时缺货，请稍后再试～";
        }
    }
}
```

#### 4. 注册 Tool Bean

编辑 `src/main/java/com/aiwork/helper/config/ToolCallbackConfig.java`:

```java
/**
 * 为 JokeTools 注册 Tool 回调
 */
@Bean
public MethodToolCallbackProvider jokeToolsCallbacks(com.aiwork.helper.ai.tools.JokeTools jokeTools) {
    log.info("注册 JokeTools Tool 回调 (笑话 Skill)");
    return MethodToolCallbackProvider.builder()
            .toolObjects(jokeTools)
            .build();
}
```

#### 5. 更新配置

编辑 `application.yml`:

```yaml
ai:
  skills:
    enabled-skills:
      - amap-weather
      - joke-teller  # 添加新 Skill
```

#### 6. 重启并测试

重启项目后，测试笑话功能：

```bash
curl -X POST http://localhost:8888/v1/chat/ai \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user",
    "message": "给我讲个笑话",
    "relationId": "group-001"
  }'
```

**预期响应**:
```
😄 来听个笑话：

程序员 A：'你结婚吗？' 程序员 B：'不结，我还没找到对象。' 程序员 A：'我也没找到。'

希望这个笑话能让你开心！
```

## 📋 检查清单

创建新 Skill 的完整流程：

- [ ] 在 `skills/` 目录下创建 Skill 文件夹
- [ ] 编写 `SKILL.md` 文件（包含 YAML Front Matter）
- [ ] 创建对应的 Tool 实现类（使用 `@Tool` 注解）
- [ ] 在 `ToolCallbackConfig` 中注册 Bean
- [ ] 在 `application.yml` 中添加到 `enabled-skills` 列表
- [ ] 重启应用
- [ ] 测试 Skill 调用

## 🔍 调试技巧

### 查看 Skill 是否加载成功

```bash
# 查看启动日志
grep "加载 Skill" logs/aiworkhelper.log

# 或使用 docker logs（如果是容器部署）
docker logs aiworkhelper-java | grep "Skill"
```

### 测试特定 Skill

```bash
# 天气查询
curl -X POST http://localhost:8888/v1/chat/ai \
  -d '{"userId":"test","message":"上海天气怎么样？","relationId":"g1"}'

# 笑话查询
curl -X POST http://localhost:8888/v1/chat/ai \
  -d '{"userId":"test","message":"讲个笑话","relationId":"g1"}'
```

## ⚡ 常见问题

### Q1: Skill 没有被加载？

**检查点**:
1. SKILL.md 文件格式是否正确
2. YAML Front Matter 是否包含 `name` 字段
3. `enabled-skills` 列表中是否包含该 skill
4. Tool 类是否有 `@Component` 注解
5. Bean 是否正确注册

### Q2: Tool 方法没有被调用？

**检查点**:
1. Tool 方法是否有 `@Tool` 注解
2. 方法描述是否清晰
3. 参数是否有 `@ToolParam` 注解
4. ChatClient 是否正确配置了 ToolCallbackProvider

### Q3: 如何查看详细的调用日志？

修改 `application.yml`:

```yaml
logging:
  level:
    com.aiwork.helper: DEBUG
    org.springframework.ai: DEBUG
```

## 🎓 进阶学习

完成入门后，可以学习：

1. **复杂参数处理**: 多参数、嵌套对象的 Tool 方法
2. **异步 Tool 调用**: 使用 CompletableFuture 提升性能
3. **Tool 组合**: 多个 Tool 协同工作
4. **错误恢复**: LLM 自动重试机制
5. **性能优化**: Tool 缓存、懒加载

## 📚 更多资源

- 详细文档：[docs/SKILL_USAGE.md](SKILL_USAGE.md)
- Spring AI 官方文档：https://docs.spring.io/spring-ai/reference
- 高德 API 文档：https://lbs.amap.com/

---

**祝你使用愉快！** 🎉
