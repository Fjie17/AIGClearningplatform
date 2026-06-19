package com.example.learningplatform.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.learningplatform.config.AiConfig;
import com.example.learningplatform.dto.AiMatchResult;
import com.example.learningplatform.dto.ResourcePageContent;
import com.example.learningplatform.entity.KnowledgePoint;
import com.example.learningplatform.entity.Subject;
import com.example.learningplatform.repository.KnowledgePointRepository;
import com.example.learningplatform.repository.SubjectRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class AiMatchService {

    private static final Logger logger = Logger.getLogger(AiMatchService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    @Autowired
    private ResourceUrlContentFetcher resourceUrlContentFetcher;

    @Autowired
    private AiMatchVersionManager versionManager;

    public Long matchSubject(String title, List<String> tags) {
        List<Subject> allSubjects = subjectRepository.findAll();
        
        if (allSubjects.isEmpty()) {
            logger.warning("数据库中没有科目数据");
            return null;
        }

        StringBuilder subjectsList = new StringBuilder();
        for (Subject subject : allSubjects) {
            subjectsList.append("- ").append(subject.getId()).append(": ").append(subject.getName());
            if (subject.getCategory() != null) {
                subjectsList.append(" (").append(subject.getCategory()).append(")");
            }
            subjectsList.append("\n");
        }

        StringBuilder tagsStr = new StringBuilder();
        if (tags != null && !tags.isEmpty()) {
            tagsStr.append("标签: ").append(String.join(", ", tags));
        }

        String prompt = String.format("""
                我有一个学习资源，请帮我判断它属于哪个学科。
                
                资源标题: %s
                %s
                
                可选的学科列表:
                %s
                
                请只返回匹配的学科ID数字，如果无法确定请返回 -1。
                """, title, tagsStr, subjectsList);

        try {
            String response = callAiApi(prompt);
            
            if (response != null && !response.isEmpty()) {
                response = response.trim();
                
                if (response.contains("-1")) {
                    logger.info("AI无法匹配科目，尝试本地匹配");
                    return localMatchSubject(title, tags, allSubjects);
                }
                
                try {
                    Long subjectId = Long.parseLong(response.replaceAll("[^0-9]", ""));
                    
                    if (subjectId > 0) {
                        boolean exists = allSubjects.stream()
                                .anyMatch(s -> s.getId().equals(subjectId));
                        if (exists) {
                            logger.info(String.format("AI匹配科目成功: subjectId=%d", subjectId));
                            return subjectId;
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.warning("AI返回格式错误: " + response);
                }
            }
        } catch (Exception e) {
            logger.warning("调用AI API失败，使用本地匹配: " + e.getMessage());
        }

        return localMatchSubject(title, tags, allSubjects);
    }

    public Long matchKnowledgePoint(Long subjectId, String title, List<String> tags) {
        if (subjectId == null) {
            return null;
        }

        List<KnowledgePoint> knowledgePoints = knowledgePointRepository.findBySubjectId(subjectId);
        
        if (knowledgePoints.isEmpty()) {
            logger.warning("该科目下没有知识点数据: subjectId=" + subjectId);
            return null;
        }

        StringBuilder kpList = new StringBuilder();
        for (KnowledgePoint kp : knowledgePoints) {
            kpList.append("- ").append(kp.getId()).append(": ").append(kp.getName());
            if (kp.getParentId() != null) {
                kpList.append(" (父知识点)");
            }
            kpList.append("\n");
        }

        StringBuilder tagsStr = new StringBuilder();
        if (tags != null && !tags.isEmpty()) {
            tagsStr.append("标签: ").append(String.join(", ", tags));
        }

        String prompt = String.format("""
                我有一个学习资源，请帮我匹配它对应的知识点。
                
                资源标题: %s
                %s
                
                该学科下的知识点列表:
                %s
                
                请只返回最匹配的知识点ID数字，如果无法确定请返回 -1。
                """, title, tagsStr, kpList);

        try {
            String response = callAiApi(prompt);
            
            if (response != null && !response.isEmpty()) {
                response = response.trim();
                
                if (response.contains("-1")) {
                    logger.info("AI无法匹配知识点，尝试本地匹配");
                    return localMatchKnowledgePoint(knowledgePoints, title, tags);
                }
                
                try {
                    Long kpId = Long.parseLong(response.replaceAll("[^0-9]", ""));
                    
                    if (kpId > 0) {
                        boolean exists = knowledgePoints.stream()
                                .anyMatch(kp -> kp.getId().equals(kpId));
                        if (exists) {
                            logger.info(String.format("AI匹配知识点成功: knowledgePointId=%d", kpId));
                            return kpId;
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.warning("AI返回格式错误: " + response);
                }
            }
        } catch (Exception e) {
            logger.warning("调用AI API失败，使用本地匹配: " + e.getMessage());
        }

        return localMatchKnowledgePoint(knowledgePoints, title, tags);
    }

    public String callAiApi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + aiConfig.getApiKey());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("temperature", aiConfig.getTemperature());
        requestBody.put("max_tokens", aiConfig.getMaxTokens());

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        String apiUrl = aiConfig.getChatCompletionsUrl();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                if (body != null && body.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                    if (!choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        Map<String, String> message = (Map<String, String>) choice.get("message");
                        return message.get("content");
                    }
                }
            }
        } catch (RestClientException e) {
            logger.warning("AI API调用异常 [" + apiUrl + "]: " + e.getMessage());
        }

        return null;
    }

    private Long localMatchSubject(String title, List<String> tags, List<Subject> subjects) {
        if (title == null || title.isEmpty()) {
            return null;
        }

        for (Subject subject : subjects) {
            String subjectName = subject.getName();
            String subjectCode = subject.getCode();
            String subjectCategory = subject.getCategory();

            if (title.contains(subjectName) || subjectName.contains(title)) {
                return subject.getId();
            }
            
            if (subjectCode != null && title.contains(subjectCode)) {
                return subject.getId();
            }
            
            if (subjectCategory != null && title.contains(subjectCategory)) {
                return subject.getId();
            }

            if (tags != null) {
                for (String tag : tags) {
                    if (tag.contains(subjectName) || subjectName.contains(tag)) {
                        return subject.getId();
                    }
                }
            }
        }

        if (tags != null && !tags.isEmpty()) {
            for (Subject subject : subjects) {
                for (String tag : tags) {
                    if (subject.getName().contains(tag.trim()) || tag.trim().contains(subject.getName())) {
                        return subject.getId();
                    }
                }
            }
        }

        return null;
    }

    private Long localMatchKnowledgePoint(List<KnowledgePoint> knowledgePoints, String title, List<String> tags) {
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                String trimmedTag = tag.trim();
                
                for (KnowledgePoint kp : knowledgePoints) {
                    if (kp.getName() != null && kp.getName().equalsIgnoreCase(trimmedTag)) {
                        return kp.getId();
                    }
                }

                Optional<KnowledgePoint> exactMatch = knowledgePoints.stream()
                        .filter(kp -> kp.getName() != null && kp.getName().equalsIgnoreCase(trimmedTag))
                        .findFirst();
                if (exactMatch.isPresent()) {
                    return exactMatch.get().getId();
                }
            }
        }

        if (title != null && !title.isEmpty()) {
            for (KnowledgePoint kp : knowledgePoints) {
                if (kp.getName() != null && title.contains(kp.getName())) {
                    return kp.getId();
                }
            }
        }

        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                String trimmedTag = tag.trim();
                for (KnowledgePoint kp : knowledgePoints) {
                    if (kp.getName() != null) {
                        if (kp.getName().contains(trimmedTag) || trimmedTag.contains(kp.getName())) {
                            return kp.getId();
                        }
                    }
                }
            }
        }

        return null;
    }

    public AiMatchResult processResource(String title, String topic, String link, String platform, Long subjectId) {
        return processResource(title, topic, link, platform, subjectId, null, null, null);
    }

    public AiMatchResult processResource(String title, String topic, String link, String platform,
            Long subjectId, String resourceType, Integer duration, Integer classHours) {
        List<KnowledgePoint> knowledgePoints = knowledgePointRepository.findBySubjectId(subjectId);
        return executeResourceMatch(title, topic, link, platform, subjectId, resourceType, duration, classHours,
                knowledgePoints, versionManager.getCurrentVersion(),
                "开始调用AI接口，资源标题: " + title + ", 链接: " + link);
    }
    
    public AiMatchResult processResourceWithKnowledgePoints(String title, String topic, String link, String platform,
            List<KnowledgePoint> knowledgePoints) {
        return processResourceWithKnowledgePoints(title, topic, link, platform, knowledgePoints,
                versionManager.getCurrentVersion());
    }

    public AiMatchResult processResourceWithKnowledgePoints(String title, String topic, String link, String platform,
            List<KnowledgePoint> knowledgePoints, String matchVersion) {
        if (knowledgePoints == null || knowledgePoints.isEmpty()) {
            logger.warning("知识点列表为空");
            return new AiMatchResult();
        }

        Long subjectId = knowledgePoints.get(0).getSubjectId();
        return executeResourceMatch(title, topic, link, platform, subjectId, null, null, null,
                knowledgePoints, matchVersion,
                "开始调用AI接口（使用传入的知识点列表），资源标题: " + title + ", 链接: " + link);
    }

    private AiMatchResult executeResourceMatch(String title, String topic, String resourceUrl, String platform,
            Long subjectId, String resourceType, Integer duration, Integer classHours,
            List<KnowledgePoint> knowledgePoints, String matchVersion, String logPrefix) {
        ResourcePageContent pageContent = fetchPageContent(resourceUrl);
        String kpJson = buildKnowledgePointJson(knowledgePoints);
        String prompt = buildResourceMatchPrompt(kpJson, platform, title, topic, resourceType, duration, classHours,
                pageContent);

        try {
            logger.info(logPrefix);
            String aiResponse = callAiApi(prompt);
            logger.info("AI调用完成，响应: " + (aiResponse != null && aiResponse.length() > 100
                    ? aiResponse.substring(0, 100) + "..." : aiResponse));

            AiMatchResult parsed = parseAiMatchResponse(aiResponse, knowledgePoints, title, subjectId, platform,
                    pageContent, matchVersion);
            if (parsed != null) {
                return parsed;
            }
        } catch (Exception e) {
            logger.warning("调用AI API失败: " + e.getMessage());
        }

        AiMatchResult fallback = processResourceFallback(title, topic, resourceUrl, platform, subjectId, resourceType,
                pageContent, matchVersion);
        return fallback;
    }

    private ResourcePageContent fetchPageContent(String resourceUrl) {
        if (resourceUrl == null || resourceUrl.isBlank()) {
            ResourcePageContent empty = new ResourcePageContent();
            empty.setFetchSuccess(false);
            empty.setErrorMessage("未提供资源链接");
            return empty;
        }

        logger.info("开始抓取资源链接页面内容: " + resourceUrl);
        long startMs = System.currentTimeMillis();
        ResourcePageContent content = resourceUrlContentFetcher.fetch(resourceUrl);
        logger.info("链接抓取完成，耗时 " + (System.currentTimeMillis() - startMs) + " ms，成功="
                + content.isFetchSuccess());
        return content;
    }

    private String buildKnowledgePointJson(List<KnowledgePoint> knowledgePoints) {
        StringBuilder kpJson = new StringBuilder("[");
        for (int i = 0; i < knowledgePoints.size(); i++) {
            KnowledgePoint kp = knowledgePoints.get(i);
            if (i > 0) {
                kpJson.append(",");
            }
            kpJson.append("{\"id\":").append(kp.getId()).append(",\"name\":\"").append(kp.getName()).append("\"}");
        }
        kpJson.append("]");
        return kpJson.toString();
    }

    /**
     * 构建资源 AI 匹配 Prompt。
     * tags 描述资源内容；ai_match_confidence 按 LQAI 四维学格（水平/速度/偏好/自律）+ 学习特征标注，供后续个性化推荐。
     */
    private String buildResourceMatchPrompt(String kpJson, String platform, String title, String topic,
            String resourceType, Integer duration, Integer classHours, ResourcePageContent pageContent) {
        String durationInfo = duration != null ? duration + "分钟" : "未知";
        String classHoursInfo = classHours != null ? classHours + "课时" : "未知";
        String pageSection = pageContent != null ? pageContent.toPromptSection() : "无";

        return String.format("""
                你是「AIGC 学习平台」的教育资源分析与学格匹配助手。请根据资源链接页面识别到的实际内容，完成知识点匹配，并生成可用于 LQAI 学格个性化推荐的标准化标签。

                ## 一、任务说明
                1. **首要依据**：【从资源链接抓取的页面实况】—— 你必须基于链接页面中识别到的标题、简介、标签、正文摘要来判断资源真实内容。
                2. 爬虫元数据（标题/话题等）仅作辅助参考，若与页面实况冲突，以页面实况为准。
                3. 从【候选知识点列表】中选出关联度最高的 1 个知识点 ID。
                4. 生成 tags、ai_match_confidence、description。

                ## 二、候选知识点列表（只能从中选择，禁止编造 ID）
                %s

                ## 三、从资源链接抓取的页面实况（AI 匹配首要依据）
                %s

                ## 四、爬虫/平台元数据（辅助参考）
                - 平台：%s
                - 资源类型：%s
                - 爬虫标题：%s
                - 爬虫话题/简介：%s
                - 爬虫时长：%s
                - 爬虫课时：%s

                ## 五、知识点匹配规则
                - 综合页面实况与元数据判断资源核心教学内容。
                - 若标题较宽泛（如「全集」「入门到精通」），以页面简介/正文摘要确定最相关的单一知识点。
                - 只能从候选列表中选 1 个 ID；页面内容不足以判断时 knowledge_point_id 返回 -1。

                ## 六、tags 生成规则（内容标签，最多 5 个，英文逗号分隔）
                基于页面实际内容，不要重复 ai_match_confidence 的学格维度词。
                - 2~3 个：与知识点/主题直接相关的核心词
                - 1~2 个：内容形态或场景词（如「例题讲解」「公式推导」「刷题训练」）
                要求：每个标签 2~8 字，名词或短语。

                ## 七、ai_match_confidence 生成规则（学格匹配标签，固定 5 个，英文逗号分隔）
                按顺序从下列选项中各选 1 个：
                第1项-水平适配：入门 | 基础 | 中等 | 进阶 | 高级
                第2项-节奏适配：速成突击 | 快节奏 | 适中 | 系统讲解 | 慢节奏深度
                第3项-形式适配：视频向 | 文本向 | 音频向 | 实践向
                第4项-自律适配：需高自律 | 中等自律 | 低自律友好 | 可碎片化 | 适合突击
                第5项-学习特征：易错点多 | 概念抽象 | 计算密集 | 记忆为主 | 理解为主 | 综合应用

                ## 八、输出格式（仅输出 JSON，无 Markdown、无解释）
                {
                  "knowledge_point_id": 数字或-1,
                  "tags": "标签1,标签2,标签3",
                  "ai_match_confidence": "水平词,节奏词,形式词,自律词,特征词",
                  "description": "不超过50字的简介",
                  "match_dimensions": {
                    "level": "与ai_match_confidence第1项相同",
                    "pace": "与第2项相同",
                    "format": "与第3项相同",
                    "discipline": "与第4项相同",
                    "trait": "与第5项相同"
                  }
                }
                """,
                kpJson,
                pageSection,
                platform != null ? platform : "未知",
                resourceType != null ? resourceType : "未知",
                title != null ? title : "",
                topic != null ? topic : "无",
                durationInfo,
                classHoursInfo);
    }

    private AiMatchResult parseAiMatchResponse(String aiResponse, List<KnowledgePoint> knowledgePoints,
            String title, Long subjectId, String platform, ResourcePageContent pageContent, String matchVersion) {
        if (aiResponse == null || aiResponse.isEmpty()) {
            logger.warning("AI返回为空");
            return null;
        }

        aiResponse = cleanAiJsonResponse(aiResponse);

        try {
            JsonNode jsonNode = objectMapper.readTree(aiResponse);

            long kpId = jsonNode.has("knowledge_point_id") ? jsonNode.get("knowledge_point_id").asLong(-1) : -1;
            Long finalKpId = resolveKnowledgePointId(kpId, knowledgePoints, title);

            String kpName = null;
            if (finalKpId != null) {
                long tempId = finalKpId;
                kpName = knowledgePoints.stream()
                        .filter(kp -> kp.getId().equals(tempId))
                        .map(KnowledgePoint::getName)
                        .findFirst()
                        .orElse(null);
            }

            AiMatchResult result = new AiMatchResult();
            result.setKnowledgePointId(finalKpId);
            result.setKnowledgePointName(kpName);

            String tags = jsonNode.has("tags") ? jsonNode.get("tags").asText("") : "";
            result.setTags(truncateTags(tags, 5));

            String confidence = jsonNode.has("ai_match_confidence") ? jsonNode.get("ai_match_confidence").asText("") : "";
            result.setConfidence(truncateTags(confidence, 5));

            String description = jsonNode.has("description") ? jsonNode.get("description").asText("") : "";
            if (description.length() > 50) {
                description = description.substring(0, 50);
            }
            result.setDescription(description);

            result.setMatchInfo(buildMatchInfoJson(subjectId, finalKpId, kpName, platform, jsonNode, pageContent,
                    matchVersion));
            result.setAiMatchVersion(matchVersion);

            logger.info("AI处理完成，知识点ID: " + finalKpId + ", 知识点名称: " + kpName + ", 标签: " + result.getTags());
            return result;
        } catch (Exception e) {
            logger.warning("解析AI返回的JSON失败: " + e.getMessage() + ", response: " + aiResponse);
            return null;
        }
    }

    private String cleanAiJsonResponse(String aiResponse) {
        aiResponse = aiResponse.trim();
        if (aiResponse.startsWith("```json")) {
            aiResponse = aiResponse.substring(7);
        } else if (aiResponse.startsWith("```")) {
            aiResponse = aiResponse.substring(3);
        }
        if (aiResponse.endsWith("```")) {
            aiResponse = aiResponse.substring(0, aiResponse.length() - 3);
        }
        return aiResponse.trim();
    }

    private Long resolveKnowledgePointId(long kpId, List<KnowledgePoint> knowledgePoints, String title) {
        if (kpId == -1 || kpId == 0) {
            logger.info("AI返回-1或0，使用本地匹配");
            return localMatchKnowledgePoint(knowledgePoints, title, extractTags(title));
        }

        long finalId = kpId;
        boolean exists = knowledgePoints.stream().anyMatch(kp -> kp.getId().equals(finalId));
        if (exists) {
            logger.info("AI返回的知识点ID有效: " + kpId);
            return kpId;
        }

        logger.warning("AI返回的知识点ID不存在于列表中: " + kpId + "，使用本地匹配");
        return localMatchKnowledgePoint(knowledgePoints, title, extractTags(title));
    }

    private String buildMatchInfoJson(Long subjectId, Long knowledgePointId, String knowledgePointName,
            String platform, JsonNode jsonNode, ResourcePageContent pageContent, String matchVersion) {
        String level = "";
        String pace = "";
        String format = "";
        String discipline = "";
        String trait = "";

        if (jsonNode.has("match_dimensions")) {
            JsonNode dims = jsonNode.get("match_dimensions");
            level = dims.has("level") ? dims.get("level").asText("") : "";
            pace = dims.has("pace") ? dims.get("pace").asText("") : "";
            format = dims.has("format") ? dims.get("format").asText("") : "";
            discipline = dims.has("discipline") ? dims.get("discipline").asText("") : "";
            trait = dims.has("trait") ? dims.get("trait").asText("") : "";
        }

        String urlFetchSuccess = pageContent != null && pageContent.isFetchSuccess() ? "true" : "false";
        String urlFetchSource = pageContent != null && pageContent.getFetchSource() != null
                ? pageContent.getFetchSource() : "";

        return String.format(
                "{\"subjectId\":%d,\"knowledgePointId\":%d,\"knowledgePointName\":\"%s\",\"platform\":\"%s\","
                        + "\"aiMatchVersion\":\"%s\",\"urlFetchSuccess\":%s,\"urlFetchSource\":\"%s\","
                        + "\"matchDimensions\":{\"level\":\"%s\",\"pace\":\"%s\",\"format\":\"%s\",\"discipline\":\"%s\",\"trait\":\"%s\"}}",
                subjectId != null ? subjectId : 0,
                knowledgePointId != null ? knowledgePointId : 0,
                escapeJson(knowledgePointName),
                escapeJson(platform),
                escapeJson(matchVersion),
                urlFetchSuccess,
                escapeJson(urlFetchSource),
                escapeJson(level),
                escapeJson(pace),
                escapeJson(format),
                escapeJson(discipline),
                escapeJson(trait));
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    
    private AiMatchResult processResourceFallback(String title, String topic, String link, String platform,
            Long subjectId, String resourceType) {
        return processResourceFallback(title, topic, link, platform, subjectId, resourceType, null,
                versionManager.getCurrentVersion());
    }

    private AiMatchResult processResourceFallback(String title, String topic, String link, String platform,
            Long subjectId, String resourceType, ResourcePageContent pageContent, String matchVersion) {
        AiMatchResult result = new AiMatchResult();
        
        List<String> tags = extractTags(title);
        
        Long knowledgePointId = matchKnowledgePoint(subjectId, title, tags);
        
        String kpName = null;
        if (knowledgePointId != null) {
            List<KnowledgePoint> knowledgePoints = knowledgePointRepository.findBySubjectId(subjectId);
            kpName = knowledgePoints.stream()
                    .filter(kp -> kp.getId().equals(knowledgePointId))
                    .map(KnowledgePoint::getName)
                    .findFirst()
                    .orElse(null);
        }
        
        String tagsStr = tags != null ? String.join(",", tags) : "";
        tagsStr = truncateTags(tagsStr, 5);

        String confidence = inferFallbackConfidence(title, topic, resourceType, knowledgePointId != null);

        String description = generateDescription(title, topic, platform);
        if (description.length() > 50) {
            description = description.substring(0, 50);
        }

        String matchInfo = String.format(
                "{\"subjectId\":%d,\"knowledgePointId\":%d,\"knowledgePointName\":\"%s\",\"platform\":\"%s\","
                        + "\"aiMatchVersion\":\"%s\",\"urlFetchSuccess\":%s,\"urlFetchSource\":\"%s\","
                        + "\"matchDimensions\":{\"level\":\"%s\",\"pace\":\"%s\",\"format\":\"%s\",\"discipline\":\"%s\",\"trait\":\"%s\"},"
                        + "\"fallback\":true}",
                subjectId, knowledgePointId != null ? knowledgePointId : 0,
                escapeJson(kpName), escapeJson(platform), escapeJson(matchVersion),
                pageContent != null && pageContent.isFetchSuccess() ? "true" : "false",
                escapeJson(pageContent != null ? pageContent.getFetchSource() : ""),
                escapeJson(confidence.split(",")[0]),
                escapeJson(confidence.split(",").length > 1 ? confidence.split(",")[1] : "适中"),
                escapeJson(confidence.split(",").length > 2 ? confidence.split(",")[2] : "视频向"),
                escapeJson(confidence.split(",").length > 3 ? confidence.split(",")[3] : "中等自律"),
                escapeJson(confidence.split(",").length > 4 ? confidence.split(",")[4] : "理解为主"));
        
        result.setKnowledgePointId(knowledgePointId);
        result.setKnowledgePointName(kpName);
        result.setTags(tagsStr);
        result.setConfidence(confidence);
        result.setDescription(description);
        result.setMatchInfo(matchInfo);
        result.setAiMatchVersion(matchVersion);
        
        return result;
    }

    private String inferFallbackConfidence(String title, String topic, String resourceType, boolean matched) {
        String combined = ((title != null ? title : "") + " " + (topic != null ? topic : "")).toLowerCase();

        String level = combined.matches(".*(入门|零基础|基础|初级).*") ? "基础"
                : combined.matches(".*(进阶|高级|竞赛|专题|深入).*") ? "进阶" : matched ? "中等" : "入门";
        String pace = combined.matches(".*(速成|冲刺|突击|三天|速通).*") ? "速成突击"
                : combined.matches(".*(详解|推导|系统|完整|精讲).*") ? "系统讲解" : "适中";
        String format = "视频向";
        if (resourceType != null) {
            String type = resourceType.toLowerCase();
            if (type.contains("doc") || type.contains("text") || type.contains("article") || type.contains("pdf")) {
                format = "文本向";
            } else if (type.contains("audio")) {
                format = "音频向";
            } else if (type.contains("exercise") || type.contains("practice") || type.contains("quiz")) {
                format = "实践向";
            }
        }
        String discipline = combined.matches(".*(速成|冲刺|突击).*") ? "适合突击" : "中等自律";
        String trait = combined.matches(".*(易错|陷阱|常见错误).*") ? "易错点多"
                : combined.matches(".*(计算|公式|运算).*") ? "计算密集" : "理解为主";

        return level + "," + pace + "," + format + "," + discipline + "," + trait;
    }
    
    private List<String> extractTags(String title) {
        List<String> tags = new ArrayList<>();
        
        if (title != null && !title.isEmpty()) {
            String[] titleParts = title.split("[\\s\\-_·.，,。]");
            for (String part : titleParts) {
                String trimmed = part.trim();
                if (trimmed.length() >= 2 && trimmed.length() <= 10) {
                    tags.add(trimmed);
                }
            }
        }
        
        if (tags.size() > 5) {
            tags = tags.subList(0, 5);
        }
        
        return tags;
    }
    
    private String truncateTags(String tags, int maxCount) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        
        String[] tagArray = tags.split(",");
        if (tagArray.length <= maxCount) {
            return tags;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < maxCount; i++) {
            if (i > 0) {
                result.append(",");
            }
            result.append(tagArray[i].trim());
        }
        
        return result.toString();
    }
    
    private String generateDescription(String title, String topic, String platform) {
        StringBuilder desc = new StringBuilder();
        
        if (platform != null && !platform.isEmpty()) {
            desc.append("来自").append(platform).append("平台的学习资源");
        } else {
            desc.append("学习资源");
        }
        
        if (title != null && !title.isEmpty()) {
            desc.append("，标题为《").append(title).append("》");
        }
        
        if (topic != null && !topic.isEmpty() && desc.length() < 40) {
            desc.append("，").append(topic.length() > 15 ? topic.substring(0, 15) : topic);
        }
        
        desc.append("。");
        
        return desc.toString();
    }
}