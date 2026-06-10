package com.example.learningplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LearningResourceDTO {
    private Long id;
    
    @NotNull(message = "所属科目不能为空")
    private Long subjectId;
    
    private String subjectName;
    
    private Long knowledgePointId;
    
    private String knowledgePointName;
    
    private String platform;
    
    @NotBlank(message = "资源标题不能为空")
    private String title;
    
    @NotBlank(message = "资源链接不能为空")
    private String resourceUrl;
    
    private String resourceType;
    private Integer duration;

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
    private String aiMatchInfo;         // AI匹配详细信息
    private String tags;                // 标签，逗号分隔
    private String description;         // 资源描述

    // ==================================
    // getter/setter
    // ==================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    
    public Long getKnowledgePointId() { return knowledgePointId; }
    public void setKnowledgePointId(Long knowledgePointId) { this.knowledgePointId = knowledgePointId; }
    
    public String getKnowledgePointName() { return knowledgePointName; }
    public void setKnowledgePointName(String knowledgePointName) { this.knowledgePointName = knowledgePointName; }
    
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getResourceUrl() { return resourceUrl; }
    public void setResourceUrl(String resourceUrl) { this.resourceUrl = resourceUrl; }
    
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

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

    public String getAiMatchInfo() { return aiMatchInfo; }
    public void setAiMatchInfo(String aiMatchInfo) { this.aiMatchInfo = aiMatchInfo; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
