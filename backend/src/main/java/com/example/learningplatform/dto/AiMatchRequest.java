package com.example.learningplatform.dto;

public class AiMatchRequest {
    private String title;
    private String topic;
    private String platform;
    private Long subjectId;
    private String resourceUrl;

    // getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public String getResourceUrl() { return resourceUrl; }
    public void setResourceUrl(String resourceUrl) { this.resourceUrl = resourceUrl; }
}