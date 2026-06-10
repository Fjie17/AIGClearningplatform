package com.example.learningplatform.dto;

public class AiMatchResult {
    private Long knowledgePointId;
    private String knowledgePointName;
    private String tags;
    private String confidence;
    private String matchInfo;
    private String description;

    public Long getKnowledgePointId() { return knowledgePointId; }
    public void setKnowledgePointId(Long knowledgePointId) { this.knowledgePointId = knowledgePointId; }
    public String getKnowledgePointName() { return knowledgePointName; }
    public void setKnowledgePointName(String knowledgePointName) { this.knowledgePointName = knowledgePointName; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }
    public String getMatchInfo() { return matchInfo; }
    public void setMatchInfo(String matchInfo) { this.matchInfo = matchInfo; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}