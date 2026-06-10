package com.example.learningplatform.dto;

import java.util.List;

public class PreviewResourceDTO {
    
    private int rowIndex;
    private String platform;
    private String title;
    private String resourceUrl;
    private List<String> tags;
    private String description;
    private Long matchedSubjectId;
    private String matchedSubjectName;
    private Long matchedKnowledgePointId;
    private String matchedKnowledgePointName;
    private String matchConfidence;
    private boolean canModify;

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
    private Long subjectId;            // 学科ID
    private String resourceType;       // 资源类型
    private Integer duration;          // 时长(分钟)

    public int getRowIndex() { return rowIndex; }
    public void setRowIndex(int rowIndex) { this.rowIndex = rowIndex; }

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

    public Long getMatchedSubjectId() { return matchedSubjectId; }
    public void setMatchedSubjectId(Long matchedSubjectId) { this.matchedSubjectId = matchedSubjectId; }

    public String getMatchedSubjectName() { return matchedSubjectName; }
    public void setMatchedSubjectName(String matchedSubjectName) { this.matchedSubjectName = matchedSubjectName; }

    public Long getMatchedKnowledgePointId() { return matchedKnowledgePointId; }
    public void setMatchedKnowledgePointId(Long matchedKnowledgePointId) { this.matchedKnowledgePointId = matchedKnowledgePointId; }

    public String getMatchedKnowledgePointName() { return matchedKnowledgePointName; }
    public void setMatchedKnowledgePointName(String matchedKnowledgePointName) { this.matchedKnowledgePointName = matchedKnowledgePointName; }

    public String getMatchConfidence() { return matchConfidence; }
    public void setMatchConfidence(String matchConfidence) { this.matchConfidence = matchConfidence; }

    public boolean isCanModify() { return canModify; }
    public void setCanModify(boolean canModify) { this.canModify = canModify; }

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

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}