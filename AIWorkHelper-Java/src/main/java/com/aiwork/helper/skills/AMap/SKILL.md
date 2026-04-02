---
name: amap-weather
aliases: [高德天气，gaode weather, amap weather]
description: |
  Get real-time weather information for any city in China using Gaode (AMAP) API.
  Use when users:
  (1) ask "what's the weather", "weather forecast", or "temperature"
  (2) need weather data for planning, travel, or outdoor activities
  (3) want to know temperature, humidity, wind, or weather conditions
  (4) ask about weather trends or recent weather changes
  Keywords: weather, temperature, forecast, rain, wind, humidity, gaode, amap
---

# 高德天气 API 使用指南

获取中国任意城市的实时天气信息。

## 使用方法

### 方法 1: 使用 bash 命令直接调用 API

```bash
curl "https://restapi.amap.com/v3/weather/weatherInfo?key=YOUR_API_KEY&city=北京&extensions=base"
```

### 方法 2: 在 Agent 中使用

当需要查询天气时，Agent 会自动使用 `bash` 工具执行 curl 命令来获取天气数据。

示例请求：
```
查询北京市的天气
```

Agent 会执行：
```bash
curl "https://restapi.amap.com/v3/weather/weatherInfo?key=${GAODE_API_KEY}&city=110000&extensions=base"
```

## 核心能力

### 1. 实时天气查询

获取当前时刻的天气状况：
- 温度（摄氏度）
- 天气现象（晴、雨、雪、多云等）
- 风向和风力
- 湿度
- 气压

### 2. 天气预报

获取未来几天的天气趋势（使用 `extensions=all`）：
- 最高/最低温度
- 白天和夜间天气状况
- 降水概率

## 配置说明

### 环境变量

在 `.env` 文件中配置高德 API Key：

```bash
GAODE_API_KEY=5b5abc7dabdaeb801a9428bc89a21854
```

### 获取 API Key

在本地.env文件中配置GAODE_API_KEY

## API 参数说明

### 基础 URL

```
https://restapi.amap.com/v3/weather/weatherInfo
```

### 请求参数

| 参数 | 必填 | 类型 | 说明 |
|------|------|------|------|
| key | 是 | String | 用户 key |
| city | 是 | String | 城市编码（如北京=110000）或城市名 |
| extensions | 否 | String | 气象类型：base（实况）/all（预报） |

### 常见城市编码

- 北京：110000
- 上海：310000
- 广州：440100
- 深圳：440300
- 杭州：330100
- 成都：510100
- 武汉：420100
- 西安：610100
- 南京：320100
- 重庆：500000
- 天津：120000

## 最佳实践

### 1. 城市名称标准化

使用标准城市名称或城市编码：
- ✅ "110000"、"北京市"、"北京"
- 优先使用城市编码更准确

### 2. 结果解释

天气数据要结合上下文：
- 温度：给出体感建议（如"较冷，建议添衣"）
- 降水：提醒携带雨具
- 风力：提醒注意防风

### 3. 时效性说明

- 实时天气：当前时刻的数据
- 天气预报：未来 24-72 小时预测
- 建议用户出行前再次确认

## 错误处理

常见问题及解决方案：

| 问题 | 原因 | 解决方法 |
|------|------|----------|
| API Key 无效 | 未配置或配置错误 | 检查 `.env` 中的 `GAODE_API_KEY` |
| 城市不存在 | 城市名称错误 | 使用标准城市名称或编码 |
| 请求超限 | 调用过于频繁 | 降低请求频率或升级配额 |
| INVALID_USER_KEY | key 与服务不匹配 | 确保申请的是 Web 服务 API |

## 示例用法

**直接查询天气：**
```
今天北京天气怎么样？
```

**结合场景：**
```
我明天要去上海出差，那边天气怎么样？需要带什么衣服？
```

**对比多个城市：**
```
北京和上海现在的温度哪个更高？
```

**查询天气预报：**
```
这周末杭州的天气适合户外活动吗？
```

## 相关资源

- 高德开放平台文档：https://lbs.amap.com/api/webservice/guide/api/weatherinfo
- 天气 API 文档：查看实时天气和天气预报接口
- 城市编码查询：可通过地理编码 API 获取

## 注意事项

1. **API 配额**：高德天气 API 有每日调用限制，请注意合理使用
2. **数据准确性**：天气预报存在不确定性，建议作为参考
3. **更新频率**：实时天气每小时更新，预报每天更新
4. **地理覆盖**：主要支持中国大陆地区城市
5. **网络要求**：需要能够访问高德 API 服务器

---

**提示**：如果你需要更详细的天气数据（如空气质量、紫外线指数等），可以扩展此 skill 或创建专门的天气分析 agent。