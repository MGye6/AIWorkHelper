/**
 * @author: 公众号：IT 杨秀才
 * @doc:后端，AI 知识进阶，后端面试场景题大全：https://golangstar.cn/
 */
package com.aiwork.helper.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * author:  公众号：IT 杨秀才
 * 后端，AI 知识进阶，后端面试场景题大全：https://golangstar.cn/
 * Skill 注册配置类
 * 支持 Spring AI Alibaba 原生的 Skill 机制
 * 
 * 使用方式:
 * 1. 在 skills 目录下创建 SKILL.md 文件
 * 2. 定义 skill 的 name, aliases, description
 * 3. 编写 skill 的使用说明文档
 * 4. Agent 会自动加载并使用该 skill
 */
@Slf4j
@Configuration
public class SkillRegistryConfig {

    /**
     * 注册高德天气 Skill
     * 从 skills/AMap/SKILL.md 加载 skill 定义
     * 对应的 Tool 实现为 WeatherTools
     */

    /**
     * 预留更多 Skill 注册位置
     * 可以在 skills 目录下添加更多 SKILL.md 文件
     * 然后创建对应的 Tool 实现类并在此注册
     */
}
