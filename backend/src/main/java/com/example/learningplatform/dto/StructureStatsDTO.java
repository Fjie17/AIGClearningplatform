package com.example.learningplatform.dto;

import java.util.List;

public class StructureStatsDTO {
    private Long subjectId;
    private String subjectName;
    private Integer totalKnowledgePoints;
    private Integer totalResources;
    private Integer avgResourcesPerKnowledgePoint;
    private List<KnowledgePointStats> knowledgePointStats;

    public static class KnowledgePointStats {
        private Long knowledgePointId;
        private String knowledgePointName;
        private Integer resourceCount;
        private Integer difficulty;
        private Integer examWeight;

        public Long getKnowledgePointId() { return knowledgePointId; }
        public void setKnowledgePointId(Long knowledgePointId) { this.knowledgePointId = knowledgePointId; }

        public String getKnowledgePointName() { return knowledgePointName; }
        public void setKnowledgePointName(String knowledgePointName) { this.knowledgePointName = knowledgePointName; }

        public Integer getResourceCount() { return resourceCount; }
        public void setResourceCount(Integer resourceCount) { this.resourceCount = resourceCount; }

        public Integer getDifficulty() { return difficulty; }
        public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }

        public Integer getExamWeight() { return examWeight; }
        public void setExamWeight(Integer examWeight) { this.examWeight = examWeight; }
    }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Integer getTotalKnowledgePoints() { return totalKnowledgePoints; }
    public void setTotalKnowledgePoints(Integer totalKnowledgePoints) { this.totalKnowledgePoints = totalKnowledgePoints; }

    public Integer getTotalResources() { return totalResources; }
    public void setTotalResources(Integer totalResources) { this.totalResources = totalResources; }

    public Integer getAvgResourcesPerKnowledgePoint() { return avgResourcesPerKnowledgePoint; }
    public void setAvgResourcesPerKnowledgePoint(Integer avgResourcesPerKnowledgePoint) { this.avgResourcesPerKnowledgePoint = avgResourcesPerKnowledgePoint; }

    public List<KnowledgePointStats> getKnowledgePointStats() { return knowledgePointStats; }
    public void setKnowledgePointStats(List<KnowledgePointStats> knowledgePointStats) { this.knowledgePointStats = knowledgePointStats; }
}