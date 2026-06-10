package com.example.learningplatform.dto;

import java.util.List;

public class ResourceRelationsDTO {
    private Long resourceId;
    private String resourceTitle;
    private Long knowledgePointId;
    private String knowledgePointName;
    private List<RelatedResource> prerequisites;
    private List<RelatedResource> followUps;
    private List<RelatedResource> relatedResources;

    public static class RelatedResource {
        private Long id;
        private String title;
        private Long knowledgePointId;
        private String knowledgePointName;
        private String relationType;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Long getKnowledgePointId() { return knowledgePointId; }
        public void setKnowledgePointId(Long knowledgePointId) { this.knowledgePointId = knowledgePointId; }

        public String getKnowledgePointName() { return knowledgePointName; }
        public void setKnowledgePointName(String knowledgePointName) { this.knowledgePointName = knowledgePointName; }

        public String getRelationType() { return relationType; }
        public void setRelationType(String relationType) { this.relationType = relationType; }
    }

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public String getResourceTitle() { return resourceTitle; }
    public void setResourceTitle(String resourceTitle) { this.resourceTitle = resourceTitle; }

    public Long getKnowledgePointId() { return knowledgePointId; }
    public void setKnowledgePointId(Long knowledgePointId) { this.knowledgePointId = knowledgePointId; }

    public String getKnowledgePointName() { return knowledgePointName; }
    public void setKnowledgePointName(String knowledgePointName) { this.knowledgePointName = knowledgePointName; }

    public List<RelatedResource> getPrerequisites() { return prerequisites; }
    public void setPrerequisites(List<RelatedResource> prerequisites) { this.prerequisites = prerequisites; }

    public List<RelatedResource> getFollowUps() { return followUps; }
    public void setFollowUps(List<RelatedResource> followUps) { this.followUps = followUps; }

    public List<RelatedResource> getRelatedResources() { return relatedResources; }
    public void setRelatedResources(List<RelatedResource> relatedResources) { this.relatedResources = relatedResources; }
}