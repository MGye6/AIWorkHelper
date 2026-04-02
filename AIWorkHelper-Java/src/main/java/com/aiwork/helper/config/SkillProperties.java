/**
 * @author: 公众号：IT 杨秀才
 * @doc:后端，AI 知识进阶，后端面试场景题大全：https://golangstar.cn/
 */
package com.aiwork.helper.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * author:  公众号：IT 杨秀才
 * 后端，AI 知识进阶，后端面试场景题大全：https://golangstar.cn/
 * Skill 配置属性类
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.skills")
public class SkillProperties {

    /**
     * 是否启用 Skills 功能
     */
    private boolean enabled = true;

    /**
     * Skills 目录路径
     */
    private String path = "src/main/java/com/aiwork/helper/skills";

    /**
     * 启用的 Skills 列表（空则启用所有）
     */
    private List<String> enabledSkills = new ArrayList<>();

    /**
     * API 调用超时时间（秒）
     */
    private int timeout = 30;

    public SkillProperties() {
        // 默认启用高德天气 Skill
        enabledSkills.add("amap-weather");
    }
}
