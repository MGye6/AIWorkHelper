# 真正的 Spring AI Alibaba Skill 实现

## 🎯 核心理念

**不再在本地编写 Tool 工具类，而是根据 SKILL.md 文件描述自动调用外部 API！**

---

## 🔄 架构升级对比

### ❌ 旧方案（伪 Skill）

```
SKILL.md (仅作文档)
    ↓
手动编写 WeatherTools.java
    ↓
硬编码 API URL 和参数
    ↓
Function Calling
```

**问题**:
- SKILL.md 只是文档，不起实际作用
- 每个 Skill 都需要编写 Java 代码
- 扩展性差，维护成本高

### ✅ 新方案（真 Skill）

```
SKILL.md (包含 API 配置)
    ↓
SkillLoader 解析元数据和 API 配置
    ↓
SkillExecutor 动态调用外部 API
    ↓
DynamicSkillTool 统一入口
    ↓
Function Calling
```

**优势**:
- SKILL.md 是 executable 的配置
- 无需编写具体的 Tool 实现
- 只需修改 SKILL.md 即可扩展新能力

---

## 🏗️ 核心组件

### 1. **SkillLoader** (`config/SkillLoader.java`)

**功能**: 解析 SKILL.md 文件，提取 API 配置

```java
// 从 SKILL.md 中提取
ApiConfig {
    url: "https://restapi.amap.com/v3/weather/weatherInfo",
    method: "GET",
    params: [
        {name: "city", required: true, type: "String"},
        {name: "extensions", required: false, type: "String"}
    ],
    examples: ["查询北京市的天气"]
}
```

**关键改进**:
- ✅ 新增 `extractApiConfig()` 方法
- ✅ 使用正则表达式提取 curl 命令中的 URL
- ✅ 解析参数表格获取参数定义
- ✅ 返回包含 API 配置的 `SkillInfo` 对象

### 2. **SkillExecutor** (`config/SkillExecutor.java`) - 新增

**功能**: 根据 API 配置动态调用外部 API

```java
public String execute(SkillInfo skillInfo, Map<String, Object> params) {
    // 1. 构建完整的 URL
    String fullUrl = buildUrl(apiConfig.getUrl(), params);
    
    // 2. 发送 HTTP 请求
    ResponseEntity<String> response = restTemplate.exchange(
        fullUrl, HttpMethod.GET, entity, String.class
    );
    
    // 3. 处理响应
    return formatResponse(response.getBody());
}
```

**特点**:
- ✅ 自动从环境变量读取 API Key
- ✅ 支持 GET/POST 等 HTTP 方法
- ✅ 智能格式化响应结果
- ✅ 完善的错误处理

### 3. **DynamicSkillTool** (`ai/tools/DynamicSkillTool.java`) - 新增

**功能**: 统一的 Skill 调用入口

```java
@Tool(description = "Get real-time weather information...")
public String queryWeather(String city) {
    // 1. 从 SkillLoader 获取 Skill 配置
    SkillInfo weatherSkill = skillLoader.loadAllSkills()
        .stream()
        .filter(s -> "amap-weather".equals(s.getName()))
        .findFirst()
        .orElse(null);
    
    // 2. 构建参数
    Map<String, Object> params = new HashMap<>();
    params.put("city", city);
    
    // 3. 执行 Skill 调用外部 API
    return skillExecutor.execute(weatherSkill, params);
}
```

**优势**:
- ✅ 不再是硬编码的实现
- ✅ 基于 SKILL.md 配置动态执行
- ✅ LLM 根据 description 自动调用

---

## 📝 SKILL.md 文件格式升级

### 必需内容

```markdown
---
name: amap-weather
aliases: [高德天气，gaode weather]
description: |
  Get real-time weather information using Gaode API.
  Use when users ask about weather, temperature, forecast.
---

# 详细说明

## 使用方法

```bash
curl "https://restapi.amap.com/v3/weather/weatherInfo?key=YOUR_API_KEY&city=北京&extensions=base"
```

## API 参数

| 参数 | 必填 | 类型 | 说明 |
|------|------|------|------|
| key | 是 | String | API Key（自动从环境变量读取） |
| city | 是 | String | 城市名称或编码 |
| extensions | 否 | String | base=实况/all=预报 |

## 示例

**查询天气：**
```
今天北京天气怎么样？
```
```

### 关键要素

1. **curl 命令** - 提供可执行的 API 调用示例
2. **参数表格** - 定义参数名称、是否必填、类型、描述
3. **使用示例** - 帮助 LLM 理解何时调用

---

## 🚀 使用流程

### 添加新 Skill（以地图搜索为例）

#### 步骤 1: 创建 SKILL.md

```bash
mkdir -p src/main/java/com/aiwork/helper/skills/AmapSearch
vim src/main/java/com/aiwork/helper/skills/AmapSearch/SKILL.md
```

```markdown
---
name: amap-search
aliases: [高德地图搜索，地点查询]
description: |
  Search for places, addresses, or POI using Gaode Maps API.
  Use when users ask for locations, addresses, nearby places.
---

# 高德地图搜索 API

## 使用方法

```bash
curl "https://restapi.amap.com/v3/place/text?key=YOUR_API_KEY&keywords=咖啡馆&city=北京"
```

## API 参数

| 参数 | 必填 | 类型 | 说明 |
|------|------|------|------|
| keywords | 是 | String | 搜索关键词 |
| city | 否 | String | 城市名称 |
| offset | 否 | Integer | 每页条数 |

## 示例

**搜索地点：**
```
帮我找一下附近的咖啡馆
```
```

#### 步骤 2: 添加环境变量

```bash
export GAODE_API_KEY=your_api_key
```

#### 步骤 3: 重启应用

```bash
mvn spring-boot:run
```

**完成！无需编写任何 Java 代码！**

---

## 🔍 工作原理

### 1. Skill 加载阶段

```
启动应用
    ↓
SkillLoader.scanSkills()
    ↓
遍历 skills/ 目录
    ↓
解析每个 SKILL.md
    ↓
提取 Front Matter + API 配置
    ↓
保存到内存：List<SkillInfo>
```

### 2. 运行时调用阶段

```
用户："北京天气怎么样？"
    ↓
AgentService.chat()
    ↓
LLM 分析意图 → 识别需要查询天气
    ↓
Function Calling → DynamicSkillTool.queryWeather("北京")
    ↓
skillLoader.loadAllSkills() → 获取 amap-weather 配置
    ↓
skillExecutor.execute(skillInfo, {city: "北京"})
    ↓
HTTP GET → https://restapi.amap.com/v3/weather/weatherInfo?key=xxx&city=北京
    ↓
API 响应 JSON
    ↓
parseWeatherResponse() 格式化
    ↓
返回给用户
```

---

## 💡 核心创新点

### 1. **SKILL.md 即代码**

SKILL.md 不再只是文档，而是**可执行的配置**：
- 包含完整的 API 调用信息
- LLM 根据描述理解何时调用
- 系统根据配置动态执行

### 2. **零代码扩展**

添加新能力只需修改 SKILL.md：
- 修改 URL → 调用新 API
- 修改参数 → 支持新输入
- 修改描述 → 改变行为

### 3. **统一执行框架**

所有 Skill 共享同一个执行器：
- `SkillExecutor` 处理所有 HTTP 请求
- `DynamicSkillTool` 统一入口
- 无需为每个 Skill 编写代码

---

## 📊 与旧方案对比

| 特性 | 旧方案 (WeatherTools) | 新方案 (DynamicSkillTool) |
|------|---------------------|--------------------------|
| 代码量 | ~200 行 Java | ~10 行配置 |
| 扩展方式 | 编写新 Tool 类 | 修改 SKILL.md |
| API 变更 | 修改 Java 代码 | 修改 SKILL.md |
| 学习成本 | 需懂 Java | 只需懂 Markdown |
| 维护成本 | 高（多个文件） | 低（单一配置） |

---

## 🎓 技术实现细节

### API 配置提取（正则表达式）

```java
// 提取 curl 命令中的 URL
Pattern curlPattern = Pattern.compile("curl\\s+[\"']?([^\"'\\s]+)[\"']?");
Matcher curlMatcher = curlPattern.matcher(content);
if (curlMatcher.find()) {
    String url = curlMatcher.group(1);
    config.setUrl(url);
}

// 提取参数表格
Pattern tablePattern = Pattern.compile(
    "\\|\\s*([\\w]+)\\s*\\|\\s*([是/否]+)\\s*\\|\\s*(\\w+)\\s*\\|\\s*([^|]+)\\|"
);
while (tableMatcher.find()) {
    // 解析参数名、是否必填、类型、描述
}
```

### 动态 URL 构建

```java
private String buildUrl(String urlTemplate, Map<String, Object> params) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlTemplate);
    
    // 自动添加 API Key
    String apiKey = System.getenv("GAODE_API_KEY");
    builder.queryParam("key", apiKey);
    
    // 添加业务参数
    for (Map.Entry<String, Object> entry : params.entrySet()) {
        builder.queryParam(entry.getKey(), entry.getValue().toString());
    }
    
    return builder.build().toUriString();
}
```

### 智能响应格式化

```java
private String formatResponse(String jsonResponse) {
    JsonNode jsonNode = objectMapper.readTree(jsonResponse);
    
    // 检查 API 状态
    if ("1".equals(jsonNode.get("status").asText())) {
        // 成功：提取关键字段
        return parseWeatherResponse(jsonNode);
    } else {
        // 失败：返回错误信息
        return String.format("API 调用失败：%s", jsonNode.get("info").asText());
    }
}
```

---

## ⚠️ 注意事项

### 1. API Key 管理

**推荐方式**: 从环境变量读取
```bash
export GAODE_API_KEY=your_api_key
```

**配置文件方式**:
```yaml
skills:
  amap:
    api-key: ${GAODE_API_KEY}
```

### 2. SKILL.md 格式要求

- ✅ 必须包含 curl 命令示例
- ✅ 必须有参数表格
- ✅ description 要清晰说明使用场景
- ✅ aliases 提供多种叫法

### 3. 错误处理

- SkillExecutor 会捕获所有异常
- 返回友好的错误提示
- 日志记录详细错误信息

---

## 🎯 下一步计划

### 短期
- [ ] 支持 POST 请求
- [ ] 支持请求头配置
- [ ] 支持响应模板自定义

### 中期
- [ ] 支持 OAuth 认证
- [ ] 支持多 API 组合
- [ ] 支持条件判断

### 长期
- [ ] Skill 编排引擎
- [ ] 可视化 Skill 编辑器
- [ ] Skill 市场

---

## 📚 参考资源

- [Spring AI Tools](https://docs.spring.io/spring-ai/reference/api/tools.html)
- [高德 API 文档](https://lbs.amap.com/api/webservice/summary)
- [原理解析文章](docs/SKILL_ARCHITECTURE.md)

---

**真正的 Skill 机制，让扩展变得如此简单！** 🎉
