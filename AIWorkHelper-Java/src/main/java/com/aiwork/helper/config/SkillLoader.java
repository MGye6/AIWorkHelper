/**
 * @author: 公众号：IT 杨秀才
 * @doc:后端，AI 知识进阶，后端面试场景题大全：https://golangstar.cn/
 */
package com.aiwork.helper.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * author:  公众号：IT 杨秀才
 * 后端，AI 知识进阶，后端面试场景题大全：https://golangstar.cn/
 * Skill 加载器
 * 
 * 负责从 skills 目录下加载 SKILL.md 文件并解析元数据和 API 配置
 */
@Slf4j
@Component
public class SkillLoader {

    private final SkillProperties skillProperties;

    public SkillLoader(SkillProperties skillProperties) {
        this.skillProperties = skillProperties;
        log.info("SkillLoader 初始化完成，Skills 路径：{}", skillProperties.getPath());
    }

    /**
     * 加载所有启用的 Skills
     * 
     * @return Skill 信息列表
     */
    public List<SkillInfo> loadAllSkills() {
        if (!skillProperties.isEnabled()) {
            log.info("Skills 功能已禁用");
            return new ArrayList<>();
        }

        List<SkillInfo> skills = new ArrayList<>();
        String skillsPath = skillProperties.getPath();

        try {
            Path rootPath = Paths.get(skillsPath);
            if (!Files.exists(rootPath)) {
                log.warn("Skills 目录不存在：{}", skillsPath);
                return skills;
            }

            // 遍历 skills 目录下的所有子目录
            try (Stream<Path> paths = Files.walk(rootPath)) {
                paths.filter(Files::isDirectory)
                     .filter(path -> !path.equals(rootPath))
                     .forEach(dir -> {
                         Path skillMdPath = dir.resolve("SKILL.md");
                         if (Files.exists(skillMdPath)) {
                             try {
                                 SkillInfo skillInfo = parseSkillMd(skillMdPath);
                                 if (skillInfo != null) {
                                     // 检查是否在启用列表中
                                     if (skillProperties.getEnabledSkills().contains(skillInfo.getName()) ||
                                         skillProperties.getEnabledSkills().isEmpty()) {
                                         skills.add(skillInfo);
                                         log.info("加载 Skill: {} ({})", skillInfo.getName(), skillInfo.getDescription());
                                     }
                                 }
                             } catch (IOException e) {
                                 log.error("读取 Skill 文件失败：{}", skillMdPath, e);
                             }
                         }
                     });
            }

            log.info("共加载 {} 个 Skills", skills.size());

        } catch (IOException e) {
            log.error("扫描 Skills 目录失败", e);
        }

        return skills;
    }

    /**
     * 解析 SKILL.md 文件
     * 
     * @param path SKILL.md 文件路径
     * @return 解析后的 Skill 信息
     */
    private SkillInfo parseSkillMd(Path path) throws IOException {
        StringBuilder content = new StringBuilder();
        StringBuilder frontMatter = new StringBuilder();
        boolean inFrontMatter = false;
        boolean frontMatterEnded = false;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null) {
                lineCount++;

                // 处理 YAML Front Matter
                if (lineCount == 1 && line.trim().equals("---")) {
                    inFrontMatter = true;
                    continue;
                }

                if (inFrontMatter) {
                    if (line.trim().equals("---")) {
                        inFrontMatter = false;
                        frontMatterEnded = true;
                        continue;
                    }
                    frontMatter.append(line).append("\n");
                } else if (frontMatterEnded) {
                    content.append(line).append("\n");
                }
            }
        }

        // 解析 Front Matter
        Map<String, String> metadata = parseFrontMatter(frontMatter.toString());

        String name = metadata.get("name");
        String description = metadata.get("description");
        String aliases = metadata.get("aliases");

        if (name == null || name.isEmpty()) {
            log.warn("Skill 缺少 name 字段：{}", path);
            return null;
        }

        // 从内容中提取 API 配置
        ApiConfig apiConfig = extractApiConfig(content.toString());

        SkillInfo skillInfo = new SkillInfo();
        skillInfo.setName(name);
        skillInfo.setDescription(description != null ? description : "");
        skillInfo.setAliases(parseAliases(aliases));
        skillInfo.setContent(content.toString().trim());
        skillInfo.setFilePath(path.toString());
        skillInfo.setApiConfig(apiConfig);

        return skillInfo;
    }

    /**
     * 从 Skill 内容中提取 API 配置
     */
    private ApiConfig extractApiConfig(String content) {
        ApiConfig config = new ApiConfig();
        
        // 提取 curl 命令中的 URL
        Pattern curlPattern = Pattern.compile("curl\\s+[\"']?([^\"'\\s]+)[\"']?");
        Matcher curlMatcher = curlPattern.matcher(content);
        if (curlMatcher.find()) {
            String url = curlMatcher.group(1);
            // 移除变量占位符
            url = url.replace("${GAODE_API_KEY}", "{api_key}")
                     .replace("${API_KEY}", "{api_key}");
            config.setUrl(url);
            config.setMethod("GET");
        }

        // 提取基础 URL（如果没有 curl）
        if (config.getUrl() == null) {
            Pattern urlPattern = Pattern.compile("https://[^\\s\"']+");
            Matcher urlMatcher = urlPattern.matcher(content);
            if (urlMatcher.find()) {
                config.setUrl(urlMatcher.group());
                config.setMethod("GET");
            }
        }

        // 提取请求参数表格
        List<ApiParam> params = new ArrayList<>();
        Pattern tablePattern = Pattern.compile(
            "\\|\\s*([\\w]+)\\s*\\|\\s*([是/否]+)\\s*\\|\\s*(\\w+)\\s*\\|\\s*([^|]+)\\|"
        );
        Matcher tableMatcher = tablePattern.matcher(content);
        while (tableMatcher.find()) {
            String paramName = tableMatcher.group(1);
            String required = tableMatcher.group(2);
            String type = tableMatcher.group(3);
            String desc = tableMatcher.group(4).trim();
            
            // 跳过 key 参数（自动从环境变量获取）
            if (!"key".equals(paramName)) {
                ApiParam param = new ApiParam();
                param.setName(paramName);
                param.setRequired("是".equals(required));
                param.setType(type);
                param.setDescription(desc);
                params.add(param);
            }
        }
        config.setParams(params);

        // 提取示例用法
        List<String> examples = new ArrayList<>();
        Pattern examplePattern = Pattern.compile("````?\n(查询 [^`]+)````?");
        Matcher exampleMatcher = examplePattern.matcher(content);
        while (exampleMatcher.find()) {
            examples.add(exampleMatcher.group(1).trim());
        }
        config.setExamples(examples);

        return config;
    }

    /**
     * 解析 YAML Front Matter
     */
    private Map<String, String> parseFrontMatter(String frontMatter) {
        Map<String, String> metadata = new HashMap<>();
        
        String[] lines = frontMatter.split("\n");
        StringBuilder currentValue = new StringBuilder();
        String currentKey = null;

        for (String line : lines) {
            if (line.contains(":") && !line.startsWith(" ") && !line.startsWith("\t")) {
                // 保存前一个键值对
                if (currentKey != null) {
                    metadata.put(currentKey, currentValue.toString().trim());
                }

                // 解析新的键值对
                int colonIndex = line.indexOf(":");
                currentKey = line.substring(0, colonIndex).trim();
                currentValue = new StringBuilder(line.substring(colonIndex + 1).trim());
            } else if (currentKey != null) {
                // 继续当前值（多行值）
                currentValue.append(" ").append(line.trim());
            }
        }

        // 保存最后一个键值对
        if (currentKey != null) {
            metadata.put(currentKey, currentValue.toString().trim());
        }

        return metadata;
    }

    /**
     * 解析别名列表
     */
    private List<String> parseAliases(String aliasesStr) {
        List<String> aliases = new ArrayList<>();
        if (aliasesStr != null && !aliasesStr.isEmpty()) {
            // 移除方括号并分割
            aliasesStr = aliasesStr.replace("[", "").replace("]", "");
            String[] parts = aliasesStr.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    aliases.add(trimmed);
                }
            }
        }
        return aliases;
    }

    /**
     * API 配置信息
     */
    @Data
    public static class ApiConfig {
        /**
         * API URL
         */
        private String url;
        
        /**
         * HTTP 方法
         */
        private String method = "GET";
        
        /**
         * 请求参数列表
         */
        private List<ApiParam> params;
        
        /**
         * 使用示例
         */
        private List<String> examples;
    }

    /**
     * API 参数定义
     */
    @Data
    public static class ApiParam {
        /**
         * 参数名称
         */
        private String name;
        
        /**
         * 是否必填
         */
        private boolean required;
        
        /**
         * 参数类型
         */
        private String type;
        
        /**
         * 参数描述
         */
        private String description;
    }

    /**
     * Skill 信息类
     */
    @Data
    public static class SkillInfo {
        /**
         * Skill 名称
         */
        private String name;

        /**
         * 描述信息
         */
        private String description;

        /**
         * 别名列表
         */
        private List<String> aliases;

        /**
         * Skill 内容
         */
        private String content;

        /**
         * 文件路径
         */
        private String filePath;

        /**
         * API 配置
         */
        private ApiConfig apiConfig;
    }
}
