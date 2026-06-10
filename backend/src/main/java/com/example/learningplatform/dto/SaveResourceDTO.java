package com.example.learningplatform.dto;

import java.util.List;

public class SaveResourceDTO {
    
    private String platform;
    private String title;
    private String resourceUrl;
    private List<String> tags;
    private String description;
    private Long subjectId;
    private Long knowledgePointId;

    // ==================================
    // 新增字段 - 爬虫数据
    // ==================================
    private String author;              // 作者
    private String views;               // 观看次数
    private Integer reviewCount;        // 评论数
    private String publishTime;         // 发布时间
    private String topic;               // 话题
    private Integer classHours;         // 课时
    private String imageUrl;            // 封面图片URL
    private String aiMatchConfidence;   // AI匹配置信度

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getResourceUrl() { return resourceUrl; }
    public void setResourceUrl(String resourceUrl) { this.resourceUrl = resourceUrl; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Long getKnowledgePointId() { return knowledgePointId; }
    public void setKnowledgePointId(Long knowledgePointId) { this.knowledgePointId = knowledgePointId; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getViews() { return views; }
    public void setViews(String views) { this.views = views; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public String getPublishTime() { return publishTime; }
    public void setPublishTime(String publishTime) { this.publishTime = publishTime; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Integer getClassHours() { return classHours; }
    public void setClassHours(Integer classHours) { this.classHours = classHours; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAiMatchConfidence() { return aiMatchConfidence; }
    public void setAiMatchConfidence(String aiMatchConfidence) { this.aiMatchConfidence = aiMatchConfidence; }
}