/**
 * @author: 公众号：IT 杨秀才
 * @doc:后端，AI 知识进阶，后端面试场景题大全：https://golangstar.cn/
 */
package com.aiwork.helper.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * author:  公众号：IT 杨秀才
 * 后端，AI 知识进阶，后端面试场景题大全：https://golangstar.cn/
 * Tool 回调配置类
 * 为现有的 Tool 工具类提供 MethodToolCallbackProvider Bean
 */
@Slf4j
@Configuration
public class ToolCallbackConfig {

    /**
     * 为 TodoTools 注册 Tool 回调
     */
    @Bean
    public MethodToolCallbackProvider todoToolsCallbacks(com.aiwork.helper.ai.tools.TodoTools todoTools) {
        log.info("注册 TodoTools Tool 回调");
        return MethodToolCallbackProvider.builder()
                .toolObjects(todoTools)
                .build();
    }

    /**
     * 为 ApprovalTools 注册 Tool 回调
     */
    @Bean
    public MethodToolCallbackProvider approvalToolsCallbacks(com.aiwork.helper.ai.tools.ApprovalTools approvalTools) {
        log.info("注册 ApprovalTools Tool 回调");
        return MethodToolCallbackProvider.builder()
                .toolObjects(approvalTools)
                .build();
    }

    /**
     * 为 KnowledgeTools 注册 Tool 回调
     */
    @Bean
    public MethodToolCallbackProvider knowledgeToolsCallbacks(com.aiwork.helper.ai.tools.KnowledgeTools knowledgeTools) {
        log.info("注册 KnowledgeTools Tool 回调");
        return MethodToolCallbackProvider.builder()
                .toolObjects(knowledgeTools)
                .build();
    }

    /**
     * 为 TimeParserTool 注册 Tool 回调
     */
    @Bean
    public MethodToolCallbackProvider timeParserToolCallbacks(com.aiwork.helper.ai.tools.TimeParserTool timeParserTool) {
        log.info("注册 TimeParserTool Tool 回调");
        return MethodToolCallbackProvider.builder()
                .toolObjects(timeParserTool)
                .build();
    }

    /**
     * 为 UserQueryTool 注册 Tool 回调
     */
    @Bean
    public MethodToolCallbackProvider userQueryToolCallbacks(com.aiwork.helper.ai.tools.UserQueryTool userQueryTool) {
        log.info("注册 UserQueryTool Tool 回调");
        return MethodToolCallbackProvider.builder()
                .toolObjects(userQueryTool)
                .build();
    }

    /**
     * 为 ChatTools 注册 Tool 回调
     */
    @Bean
    public MethodToolCallbackProvider chatToolsCallbacks(com.aiwork.helper.ai.tools.ChatTools chatTools) {
        log.info("注册 ChatTools Tool 回调");
        return MethodToolCallbackProvider.builder()
                .toolObjects(chatTools)
                .build();
    }

    /**
     * 为 FileTools 注册 Tool 回调
     */
    @Bean
    public MethodToolCallbackProvider fileToolsCallbacks(com.aiwork.helper.ai.tools.FileTools fileTools) {
        log.info("注册 FileTools Tool 回调");
        return MethodToolCallbackProvider.builder()
                .toolObjects(fileTools)
                .build();
    }

    /**
     * 为 DynamicSkillTool 注册 Tool 回调（动态 Skill 调用）
     */
    @Bean
    public MethodToolCallbackProvider dynamicSkillToolCallbacks(com.aiwork.helper.ai.tools.DynamicSkillTool dynamicSkillTool) {
        log.info("注册 DynamicSkillTool Tool 回调 (基于 SKILL.md 的动态 API 调用)");
        return MethodToolCallbackProvider.builder()
                .toolObjects(dynamicSkillTool)
                .build();
    }
}
