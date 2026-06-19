package com.example.learningplatform.dto;

public class AiRematchItemResult {

    private Long resourceId;
    private String title;
    private String resourceUrl;
    private String previousVersion;
    private String newVersion;
    private Long previousKnowledgePointId;
    private Long newKnowledgePointId;
    private String previousTags;
    private String newTags;
    private String previousConfidence;
    private String newConfidence;
    private boolean success;
    private String message;

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getPreviousVersion() {
        return previousVersion;
    }

    public void setPreviousVersion(String previousVersion) {
        this.previousVersion = previousVersion;
    }

    public String getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }

    public Long getPreviousKnowledgePointId() {
        return previousKnowledgePointId;
    }

    public void setPreviousKnowledgePointId(Long previousKnowledgePointId) {
        this.previousKnowledgePointId = previousKnowledgePointId;
    }

    public Long getNewKnowledgePointId() {
        return newKnowledgePointId;
    }

    public void setNewKnowledgePointId(Long newKnowledgePointId) {
        this.newKnowledgePointId = newKnowledgePointId;
    }

    public String getPreviousTags() {
        return previousTags;
    }

    public void setPreviousTags(String previousTags) {
        this.previousTags = previousTags;
    }

    public String getNewTags() {
        return newTags;
    }

    public void setNewTags(String newTags) {
        this.newTags = newTags;
    }

    public String getPreviousConfidence() {
        return previousConfidence;
    }

    public void setPreviousConfidence(String previousConfidence) {
        this.previousConfidence = previousConfidence;
    }

    public String getNewConfidence() {
        return newConfidence;
    }

    public void setNewConfidence(String newConfidence) {
        this.newConfidence = newConfidence;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
