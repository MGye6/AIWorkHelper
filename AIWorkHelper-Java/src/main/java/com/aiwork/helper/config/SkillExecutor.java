/**
 * @author: 公众号：IT 杨秀才
 * @doc:后端，AI 知识进阶，后端面试场景题大全：https://golangstar.cn/
 */
package com.aiwork.helper.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author:  公众号：IT 杨秀才
 * 后端，AI 知识进阶，后端面试场景题大全：https://golangstar.cn/
 * Skill 执行器
 * 
 * 根据 SkillLoader 解析的 API 配置，动态调用外部 API
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillExecutor {

    private final SkillProperties skillProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 执行 Skill 调用
     * 
     * @param skillInfo Skill 信息
     * @param params 参数值映射（参数名 -> 参数值）
     * @return API 响应结果
     */
    public String execute(SkillLoader.SkillInfo skillInfo, Map<String, Object> params) {
        log.info("执行 Skill: {}, 参数：{}", skillInfo.getName(), params);

        try {
            SkillLoader.ApiConfig apiConfig = skillInfo.getApiConfig();
            if (apiConfig == null || apiConfig.getUrl() == null) {
                return "错误：Skill 未配置 API URL，请检查 SKILL.md 文件格式";
            }

            // 构建完整的 URL
            String fullUrl = buildUrl(apiConfig.getUrl(), params);
            log.debug("请求 URL: {}", fullUrl);

            // 发送 HTTP 请求
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.GET,
                entity,
                String.class
            );

            // 处理响应
            String responseBody = response.getBody();
            log.debug("API 响应：{}", responseBody);

            if (responseBody != null) {
                // 尝试解析 JSON 并格式化
                return formatResponse(responseBody);
            } else {
                return "API 返回空响应";
            }

        } catch (Exception e) {
            log.error("Skill 执行失败：{}", skillInfo.getName(), e);
            return "Skill 执行失败：" + e.getMessage();
        }
    }

    /**
     * 构建完整的 URL（包含查询参数）
     */
    private String buildUrl(String urlTemplate, Map<String, Object> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlTemplate);

        // 添加 API Key（从环境变量读取）
        String apiKey = System.getenv("GAODE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            // 使用默认值（仅用于测试）
            apiKey = "5b5abc7dabdaeb801a9428bc89a21854";
        }
        builder.queryParam("key", apiKey);

        // 添加其他参数
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                builder.queryParam(entry.getKey(), entry.getValue().toString());
            }
        }

        // 添加 extensions 参数（默认为 base）
        if (!params.containsKey("extensions")) {
            builder.queryParam("extensions", "base");
        }

        return builder.build().toUriString();
    }

    /**
     * 格式化 API 响应
     */
    private String formatResponse(String jsonResponse) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            
            // 检查 API 状态
            JsonNode statusNode = jsonNode.get("status");
            if (statusNode != null && "1".equals(statusNode.asText())) {
                // 成功响应，提取关键信息
                return parseWeatherResponse(jsonNode);
            } else {
                // API 返回错误
                String info = jsonNode.has("info") ? jsonNode.get("info").asText() : "未知错误";
                String infocode = jsonNode.has("infocode") ? jsonNode.get("infocode").asText() : "";
                return String.format("API 调用失败：%s (代码：%s)", info, infocode);
            }
        } catch (Exception e) {
            // 不是 JSON 格式，直接返回原文
            return jsonResponse;
        }
    }

    /**
     * 解析天气 API 响应（针对高德天气 API）
     */
    private String parseWeatherResponse(JsonNode jsonNode) throws Exception {
        StringBuilder result = new StringBuilder();

        JsonNode livesNode = jsonNode.get("lives");
        if (livesNode != null && livesNode.isArray() && livesNode.size() > 0) {
            JsonNode weather = livesNode.get(0);

            String province = getFieldAsString(weather, "province");
            String city = getFieldAsString(weather, "city");
            String weatherType = getFieldAsString(weather, "weather");
            String temperature = getFieldAsString(weather, "temperature");
            String windDirection = getFieldAsString(weather, "winddirection");
            String windPower = getFieldAsString(weather, "windpower");
            String humidity = getFieldAsString(weather, "humidity");

            result.append("【").append(province).append(" ").append(city).append("实时天气】\n");
            result.append("天气：").append(weatherType).append("\n");
            result.append("温度：").append(temperature).append("℃\n");
            result.append("风向：").append(windDirection).append("\n");
            result.append("风力：").append(windPower).append("级\n");
            result.append("湿度：").append(humidity).append("%\n");

            // 添加生活建议
            result.append("\n💡 温馨提示：");
            if (weatherType.contains("雨") || weatherType.contains("雪")) {
                result.append("降水天气，请携带雨具。\n");
            }
            if (temperature != null && !temperature.isEmpty()) {
                int temp = Integer.parseInt(temperature);
                if (temp < 10) {
                    result.append("气温较低，请注意保暖。\n");
                } else if (temp > 30) {
                    result.append("气温较高，请注意防暑降温。\n");
                } else {
                    result.append("气温适宜，适合户外活动。\n");
                }
            }
        } else {
            result.append("未查询到天气信息\n");
        }

        return result.toString();
    }

    /**
     * 辅助方法：安全获取字段值
     */
    private String getFieldAsString(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : "";
    }
}
