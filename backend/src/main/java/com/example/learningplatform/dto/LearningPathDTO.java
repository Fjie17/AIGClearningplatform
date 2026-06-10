package com.example.learningplatform.dto;

import java.util.List;

public class LearningPathDTO {
    private Long subjectId;
    private String subjectName;
    private List<PathNode> nodes;
    private Integer totalDuration;
    private Integer totalResources;

    public static class PathNode {
        private Long knowledgePointId;
        private String knowledgePointName;
        private Integer difficulty;
        private Integer order;
        private List<Long> prerequisiteKnowledgePointIds;
        private List<ResourceSummary> resources;
        private String estimatedTime;

        public Long getKnowledgePointId() { return knowledgePointId; }
        public void setKnowledgePointId(Long knowledgePointId) { this.knowledgePointId = knowledgePointId; }

        public String getKnowledgePointName() { return knowledgePointName; }
        public void setKnowledgePointName(String knowledgePointName) { this.knowledgePointName = knowledgePointName; }

        public Integer getDifficulty() { return difficulty; }
        public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }

        public Integer getOrder() { return order; }
        public void setOrder(Integer order) { this.order = order; }

        public List<Long> getPrerequisiteKnowledgePointIds() { return prerequisiteKnowledgePointIds; }
        public void setPrerequisiteKnowledgePointIds(List<Long> prerequisiteKnowledgePointIds) { this.prerequisiteKnowledgePointIds = prerequisiteKnowledgePointIds; }

        public List<ResourceSummary> getResources() { return resources; }
        public void setResources(List<ResourceSummary> resources) { this.resources = resources; }

        public String getEstimatedTime() { return estimatedTime; }
        public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }
    }

    public static class ResourceSummary {
        private Long id;
        private String title;
        private String platform;
        private Integer duration;
        private String resourceType;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }

        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }

        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public List<PathNode> getNodes() { return nodes; }
    public void setNodes(List<PathNode> nodes) { this.nodes = nodes; }

    public Integer getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Integer totalDuration) { this.totalDuration = totalDuration; }

    public Integer getTotalResources() { return totalResources; }
    public void setTotalResources(Integer totalResources) { this.totalResources = totalResources; }
}