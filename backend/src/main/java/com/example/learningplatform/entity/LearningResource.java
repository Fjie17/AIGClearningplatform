package com.example.learningplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_resource")
public class LearningResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "knowledge_point_id")
    private Long knowledgePointId;

    private String platform;
    private String title;

    @Column(name = "resource_url")
    private String resourceUrl;

    @Column(name = "resource_type")
    private String resourceType;

    private Integer duration;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ==================================
    // 新增字段 - 爬虫数据
    // ==================================
    private String author;              // 作者
    private String views;               // 观看次数
    @Column(name = "review_count")
    private Integer reviewCount;        // 评论数
    @Column(name = "publish_time")
    private String publishTime;         // 发布时间
    private String topic;               // 话题
    @Column(name = "class_hours")
    private Integer classHours;         // 课时
    @Column(name = "image_url")
    private String imageUrl;            // 封面图片URL
    @Column(name = "ai_match_confidence")
    private String aiMatchConfidence;   // AI匹配置信度(high/medium/low)
    @Column(name = "ai_match_info", columnDefinition = "json")
    private String aiMatchInfo;         // AI匹配详细信息(JSON)
    private String tags;                // 标签，逗号分隔
    @Column(columnDefinition = "text")
    private String description;         // 资源描述

    @Column(name = "ai_match_version")
    private String aiMatchVersion;      // AI匹配算法版本号，默认1.0

    // ==================================
    // getter / setter
    // ==================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Long getKnowledgePointId() { return knowledgePointId; }
    public void setKnowledgePointId(Long knowledgePointId) { this.knowledgePointId = knowledgePointId; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

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

    public String getAiMatchVersion() { return aiMatchVersion; }
    public void setAiMatchVersion(String aiMatchVersion) { this.aiMatchVersion = aiMatchVersion; }
}
