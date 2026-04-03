/**
 * @author: 公众号：IT 杨秀才
 * @doc:后端，AI 知识进阶，后端面试场景题大全：https://golangstar.cn/
 */
package com.aiwork.helper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * author:  公众号：IT 杨秀才
 * 后端，AI 知识进阶，后端面试场景题大全：https://golangstar.cn/
 * DashScope 配置属性
 * 注意：新版本 Spring AI Alibaba 使用 spring.ai.dashscope 前缀
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.ai.dashscope")
public class DashScopeProperties {

    /**
     * API 密钥
     */
    private String apiKey;

    /**
     * 基础 URL
     */
    private String baseUrl;

    /**
     * 聊天配置
     */
    private Chat chat = new Chat();

    /**
     * 嵌入配置
     */
    private Embedding embedding = new Embedding();

    @Data
    public static class Chat {
        /**
         * 模型名称
         */
        private String model = "qwen-max";

        /**
         * 温度参数
         */
        private Double temperature = 0.7;
    }

    @Data
    public static class Embedding {
        /**
         * 嵌入模型名称
         */
        private String model = "text-embedding-v3";
    }
}