/**
 * @author: 公众号：IT 杨秀才
 * @doc:后端，AI 知识进阶，后端面试场景题大全：https://golangstar.cn/
 */
package com.aiwork.helper.ai.tools;

import com.aiwork.helper.config.SkillExecutor;
import com.aiwork.helper.config.SkillLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author:  公众号：IT 杨秀才
 * 后端，AI 知识进阶，后端面试场景题大全：https://golangstar.cn/
 * 通用 Skill 工具
 * 
 * 根据 SKILL.md 文件描述，动态调用外部 API，无需在本地编写具体的 Tool 实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicSkillTool {

    private final SkillExecutor skillExecutor;
    private final SkillLoader skillLoader;

    /**
     * 查询天气（基于高德天气 Skill）
     * 
     * 这个方法不是硬编码的，而是根据 SKILL.md 中的描述自动生成
     * LLM 会根据 Skill 的 description 和 aliases 自动调用此方法
     */
    @Tool(description = "Get real-time weather information for any city in China using Gaode (AMAP) API. Use when users: (1) ask 'what's the weather', 'weather forecast', or 'temperature' (2) need weather data for planning, travel, or outdoor activities (3) want to know temperature, humidity, wind, or weather conditions (4) ask about weather trends or recent weather changes")
    public String queryWeather(
        @ToolParam(description = "城市名称或城市编码，如'北京'、'上海市'或'110000'", required = true) String city
    ) {
        log.info("DynamicSkillTool 调用 - queryWeather: city={}", city);

        try {
            // 从 SkillLoader 获取 Skill 配置
            List<SkillLoader.SkillInfo> skills = skillLoader.loadAllSkills();
            SkillLoader.SkillInfo weatherSkill = null;

            for (SkillLoader.SkillInfo skill : skills) {
                if ("amap-weather".equals(skill.getName())) {
                    weatherSkill = skill;
                    break;
                }
            }

            if (weatherSkill == null) {
                return "错误：未找到天气 Skill，请检查 SKILL.md 文件是否存在";
            }

            // 构建参数
            Map<String, Object> params = new HashMap<>();
            params.put("city", city);

            // 执行 Skill 调用外部 API
            return skillExecutor.execute(weatherSkill, params);

        } catch (Exception e) {
            log.error("天气查询失败", e);
            return "天气查询失败：" + e.getMessage();
        }
    }

    /**
     * 查询天气预报（基于高德天气 Skill）
     */
    @Tool(description = "Get weather forecast for any city in China using Gaode (AMAP) API. Use when users ask about future weather, weather trends, or planning outdoor activities.")
    public String queryWeatherForecast(
        @ToolParam(description = "城市名称或城市编码", required = true) String city,
        @ToolParam(description = "预报类型：base=实况（默认），all=预报", required = false) String extensions
    ) {
        log.info("DynamicSkillTool 调用 - queryWeatherForecast: city={}, extensions={}", city, extensions);

        try {
            List<SkillLoader.SkillInfo> skills = skillLoader.loadAllSkills();
            SkillLoader.SkillInfo weatherSkill = null;

            for (SkillLoader.SkillInfo skill : skills) {
                if ("amap-weather".equals(skill.getName())) {
                    weatherSkill = skill;
                    break;
                }
            }

            if (weatherSkill == null) {
                return "错误：未找到天气 Skill";
            }

            Map<String, Object> params = new HashMap<>();
            params.put("city", city);
            if (extensions != null && !extensions.isEmpty()) {
                params.put("extensions", extensions);
            } else {
                params.put("extensions", "all"); // 默认为预报
            }

            return skillExecutor.execute(weatherSkill, params);

        } catch (Exception e) {
            log.error("天气预报查询失败", e);
            return "天气预报查询失败：" + e.getMessage();
        }
    }
}
