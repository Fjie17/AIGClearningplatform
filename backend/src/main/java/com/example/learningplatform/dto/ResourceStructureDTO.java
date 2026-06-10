package com.example.learningplatform.dto;

import java.util.List;

public class ResourceStructureDTO {
    private Long subjectId;
    private String subjectName;
    private List<KnowledgePointNode> knowledgePoints;

    public static class KnowledgePointNode {
        private Long id;
        private String name;
        private Integer difficulty;
        private Integer examWeight;
        private Long parentId;
        private List<KnowledgePointNode> children;
        private List<ResourceSummary> resources;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getDifficulty() { return difficulty; }
        public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }

        public Integer getExamWeight() { return examWeight; }
        public void setExamWeight(Integer examWeight) { this.examWeight = examWeight; }

        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }

        public List<KnowledgePointNode> getChildren() { return children; }
        public void setChildren(List<KnowledgePointNode> children) { this.children = children; }

        public List<ResourceSummary> getResources() { return resources; }
        public void setResources(List<ResourceSummary> resources) { this.resources = resources; }
    }

    public static class ResourceSummary {
        private Long id;
        private String title;
        private String platform;
        private String resourceType;
        private Integer duration;
        private String aiMatchConfidence;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }

        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }

        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }

        public String getAiMatchConfidence() { return aiMatchConfidence; }
        public void setAiMatchConfidence(String aiMatchConfidence) { this.aiMatchConfidence = aiMatchConfidence; }
    }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public List<KnowledgePointNode> getKnowledgePoints() { return knowledgePoints; }
    public void setKnowledgePoints(List<KnowledgePointNode> knowledgePoints) { this.knowledgePoints = knowledgePoints; }
}