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

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiConfig.getApiUrl() + "/chat/completions", request, Map.class);

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
            logger.warning("AI API调用异常: " + e.getMessage());
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
        AiMatchResult result = new AiMatchResult();
        
        List<KnowledgePoint> knowledgePoints = knowledgePointRepository.findBySubjectId(subjectId);
        
        StringBuilder kpJson = new StringBuilder("[");
        for (int i = 0; i < knowledgePoints.size(); i++) {
            KnowledgePoint kp = knowledgePoints.get(i);
            if (i > 0) {
                kpJson.append(",");
            }
            kpJson.append("{\"id\":").append(kp.getId()).append(",\"name\":\"").append(kp.getName()).append("\"}");
        }
        kpJson.append("]");
        
        String prompt = String.format("""
                你是一个专业的教育资源标签与知识点匹配助手。你的任务是根据用户提供的【资源标题】和【资源介绍】，从给定的【候选知识点列表】中，选出与该资源关联度最高（匹配度最高）的一个知识点的 ID，以及其他需要提炼的内容
                
                【候选知识点列表】
                %s
                
                【待分析的资源信息】
                平台：%s
                标题：%s
                简介：%s
                
                【严格输出要求】
                请仔细分析资源信息，只能从上述【候选知识点列表】中选择一个最匹配的 id。
                绝对不允许编造列表中不存在的 ID。如果找不到合适的，请返回 -1。
                最终结果请直接以 JSON 格式返回，不要包含任何额外的解释、Markdown 标记或废话。
                
                JSON 格式必须如下：
                {
                "knowledge_point_id": 选中的ID数字,
                "tags": "标签1,标签2,标签3",
                "ai_match_confidence": "适合人群1,适合人群2",
                "description": "不超过50字的精简介绍"
                }
                """, kpJson, platform != null ? platform : "", title, topic != null ? topic : "");
        
        String aiResponse = null;
        try {
            logger.info("开始调用AI接口，资源标题: " + title + ", 平台: " + platform);
            aiResponse = callAiApi(prompt);
            logger.info("AI调用完成，响应: " + (aiResponse != null && aiResponse.length() > 100 ? aiResponse.substring(0, 100) + "..." : aiResponse));
            
            if (aiResponse != null && !aiResponse.isEmpty()) {
                aiResponse = aiResponse.trim();
                
                if (aiResponse.startsWith("```json")) {
                    aiResponse = aiResponse.substring(7);
                }
                if (aiResponse.endsWith("```")) {
                    aiResponse = aiResponse.substring(0, aiResponse.length() - 3);
                }
                
                aiResponse = aiResponse.trim();
                
                try {
                    JsonNode jsonNode = objectMapper.readTree(aiResponse);
                    
                    long kpId = jsonNode.has("knowledge_point_id") ? jsonNode.get("knowledge_point_id").asLong(-1) : -1;
                    Long finalKpId = null;
                    
                    if (kpId == -1 || kpId == 0) {
                        logger.info("AI返回-1或0，使用本地匹配");
                        List<String> tags = extractTags(title);
                        finalKpId = localMatchKnowledgePoint(knowledgePoints, title, tags);
                    } else {
                        long finalId = kpId;
                        boolean exists = knowledgePoints.stream()
                                .anyMatch(kp -> kp.getId().equals(finalId));
                        if (exists) {
                            logger.info("AI返回的知识点ID有效: " + kpId);
                            finalKpId = kpId;
                        } else {
                            logger.warning("AI返回的知识点ID不存在于列表中: " + kpId + "，使用本地匹配");
                            List<String> tags = extractTags(title);
                            finalKpId = localMatchKnowledgePoint(knowledgePoints, title, tags);
                        }
                    }
                    result.setKnowledgePointId(finalKpId);
                    
                    String kpName = null;
                    if (finalKpId != null) {
                        long tempId = finalKpId;
                        kpName = knowledgePoints.stream()
                                .filter(kp -> kp.getId().equals(tempId))
                                .map(KnowledgePoint::getName)
                                .findFirst()
                                .orElse(null);
                    }
                    result.setKnowledgePointName(kpName);
                    
                    String tags = jsonNode.has("tags") ? jsonNode.get("tags").asText("") : "";
                    tags = truncateTags(tags, 5);
                    result.setTags(tags);
                    
                    String confidence = jsonNode.has("ai_match_confidence") ? jsonNode.get("ai_match_confidence").asText("") : "";
                    confidence = truncateTags(confidence, 5);
                    result.setConfidence(confidence);
                    
                    String description = jsonNode.has("description") ? jsonNode.get("description").asText("") : "";
                    if (description.length() > 50) {
                        description = description.substring(0, 50) + "...";
                    }
                    result.setDescription(description);
                    
                    String matchInfo = String.format("{\"subjectId\":%d,\"knowledgePointId\":%d,\"knowledgePointName\":\"%s\",\"platform\":\"%s\"}", 
                            subjectId, finalKpId != null ? finalKpId : 0, kpName != null ? kpName : "", platform);
                    result.setMatchInfo(matchInfo);
                    
                    logger.info("AI处理完成，知识点ID: " + finalKpId + ", 知识点名称: " + kpName + ", 标签: " + tags);
                    return result;
                } catch (Exception e) {
                    logger.warning("解析AI返回的JSON失败: " + e.getMessage() + ", response: " + aiResponse);
                }
            } else {
                logger.warning("AI返回为空");
            }
        } catch (Exception e) {
            logger.warning("调用AI API失败: " + e.getMessage());
        }
        
        return processResourceFallback(title, topic, link, platform, subjectId);
    }
    
    public AiMatchResult processResourceWithKnowledgePoints(String title, String topic, String link, String platform, List<KnowledgePoint> knowledgePoints) {
        AiMatchResult result = new AiMatchResult();
        
        if (knowledgePoints == null || knowledgePoints.isEmpty()) {
            logger.warning("知识点列表为空");
            return result;
        }
        
        Long subjectId = null;
        if (!knowledgePoints.isEmpty() && knowledgePoints.get(0).getSubjectId() != null) {
            subjectId = knowledgePoints.get(0).getSubjectId();
        }
        
        StringBuilder kpJson = new StringBuilder("[");
        for (int i = 0; i < knowledgePoints.size(); i++) {
            KnowledgePoint kp = knowledgePoints.get(i);
            if (i > 0) {
                kpJson.append(",");
            }
            kpJson.append("{\"id\":").append(kp.getId()).append(",\"name\":\"").append(kp.getName()).append("\"}");
        }
        kpJson.append("]");
        
        String prompt = String.format("""
                你是一个专业的教育资源标签与知识点匹配助手。你的任务是根据用户提供的【资源标题】和【资源介绍】，从给定的【候选知识点列表】中，选出与该资源关联度最高（匹配度最高）的一个知识点的 ID，以及其他需要提炼的内容
                
                【候选知识点列表】
                %s
                
                【待分析的资源信息】
                平台：%s
                标题：%s
                简介：%s
                
                【严格输出要求】
                请仔细分析资源信息，只能从上述【候选知识点列表】中选择一个最匹配的 id。
                绝对不允许编造列表中不存在的 ID。如果找不到合适的，请返回 -1。
                最终结果请直接以 JSON 格式返回，不要包含任何额外的解释、Markdown 标记或废话。
                
                JSON 格式必须如下：
                {
                "knowledge_point_id": 选中的ID数字,
                "tags": "标签1,标签2,标签3",
                "ai_match_confidence": "适合人群1,适合人群2",
                "description": "不超过50字的精简介绍"
                }
                """, kpJson, platform != null ? platform : "", title, topic != null ? topic : "");
        
        String aiResponse = null;
        try {
            logger.info("开始调用AI接口（使用传入的知识点列表），资源标题: " + title + ", 平台: " + platform);
            aiResponse = callAiApi(prompt);
            logger.info("AI调用完成，响应: " + (aiResponse != null && aiResponse.length() > 100 ? aiResponse.substring(0, 100) + "..." : aiResponse));
            
            if (aiResponse != null && !aiResponse.isEmpty()) {
                aiResponse = aiResponse.trim();
                
                if (aiResponse.startsWith("```json")) {
                    aiResponse = aiResponse.substring(7);
                }
                if (aiResponse.endsWith("```")) {
                    aiResponse = aiResponse.substring(0, aiResponse.length() - 3);
                }
                
                aiResponse = aiResponse.trim();
                
                try {
                    JsonNode jsonNode = objectMapper.readTree(aiResponse);
                    
                    long kpId = jsonNode.has("knowledge_point_id") ? jsonNode.get("knowledge_point_id").asLong(-1) : -1;
                    Long finalKpId = null;
                    
                    if (kpId == -1 || kpId == 0) {
                        logger.info("AI返回-1或0，使用本地匹配");
                        List<String> tags = extractTags(title);
                        finalKpId = localMatchKnowledgePoint(knowledgePoints, title, tags);
                    } else {
                        long finalId = kpId;
                        boolean exists = knowledgePoints.stream()
                                .anyMatch(kp -> kp.getId().equals(finalId));
                        if (exists) {
                            logger.info("AI返回的知识点ID有效: " + kpId);
                            finalKpId = kpId;
                        } else {
                            logger.warning("AI返回的知识点ID不存在于列表中: " + kpId + "，使用本地匹配");
                            List<String> tags = extractTags(title);
                            finalKpId = localMatchKnowledgePoint(knowledgePoints, title, tags);
                        }
                    }
                    result.setKnowledgePointId(finalKpId);
                    
                    String kpName = null;
                    if (finalKpId != null) {
                        long tempId = finalKpId;
                        kpName = knowledgePoints.stream()
                                .filter(kp -> kp.getId().equals(tempId))
                                .map(KnowledgePoint::getName)
                                .findFirst()
                                .orElse(null);
                    }
                    result.setKnowledgePointName(kpName);
                    
                    String tags = jsonNode.has("tags") ? jsonNode.get("tags").asText("") : "";
                    tags = truncateTags(tags, 5);
                    result.setTags(tags);
                    
                    String confidence = jsonNode.has("ai_match_confidence") ? jsonNode.get("ai_match_confidence").asText("") : "";
                    confidence = truncateTags(confidence, 5);
                    result.setConfidence(confidence);
                    
                    String description = jsonNode.has("description") ? jsonNode.get("description").asText("") : "";
                    if (description.length() > 50) {
                        description = description.substring(0, 50) + "...";
                    }
                    result.setDescription(description);
                    
                    String matchInfo = String.format("{\"subjectId\":%d,\"knowledgePointId\":%d,\"knowledgePointName\":\"%s\",\"platform\":\"%s\"}", 
                            subjectId != null ? subjectId : 0, finalKpId != null ? finalKpId : 0, kpName != null ? kpName : "", platform != null ? platform : "");
                    result.setMatchInfo(matchInfo);
                    
                    logger.info("AI处理完成（使用传入知识点列表），知识点ID: " + finalKpId + ", 知识点名称: " + kpName + ", 标签: " + tags);
                    return result;
                } catch (Exception e) {
                    logger.warning("解析AI返回的JSON失败: " + e.getMessage() + ", response: " + aiResponse);
                }
            } else {
                logger.warning("AI返回为空");
            }
        } catch (Exception e) {
            logger.warning("调用AI API失败: " + e.getMessage());
        }
        
        return processResourceFallback(title, topic, link, platform, subjectId);
    }
    
    private AiMatchResult processResourceFallback(String title, String topic, String link, String platform, Long subjectId) {
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
        
        String confidence = "medium";
        if (knowledgePointId != null) {
            confidence = "高难度,需要自律,易错点多,适合进阶学习者,有挑战性";
        } else {
            confidence = "中等难度,适合初学者,需要练习,基础入门,循序渐进";
        }
        
        String description = generateDescription(title, topic, platform);
        if (description.length() > 50) {
            description = description.substring(0, 50) + "...";
        }
        
        String matchInfo = String.format("{\"subjectId\":%d,\"knowledgePointId\":%d,\"knowledgePointName\":\"%s\",\"platform\":\"%s\"}", 
                subjectId, knowledgePointId != null ? knowledgePointId : 0, kpName != null ? kpName : "", platform);
        
        result.setKnowledgePointId(knowledgePointId);
        result.setKnowledgePointName(kpName);
        result.setTags(tagsStr);
        result.setConfidence(confidence);
        result.setDescription(description);
        result.setMatchInfo(matchInfo);
        
        return result;
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