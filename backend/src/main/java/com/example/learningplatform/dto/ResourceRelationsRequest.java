package com.example.learningplatform.dto;

import java.util.List;

public class ResourceRelationsRequest {
    private List<Long> prerequisiteResourceIds;
    private List<Long> followUpResourceIds;
    private List<Long> relatedResourceIds;

    public List<Long> getPrerequisiteResourceIds() { return prerequisiteResourceIds; }
    public void setPrerequisiteResourceIds(List<Long> prerequisiteResourceIds) { this.prerequisiteResourceIds = prerequisiteResourceIds; }

    public List<Long> getFollowUpResourceIds() { return followUpResourceIds; }
    public void setFollowUpResourceIds(List<Long> followUpResourceIds) { this.followUpResourceIds = followUpResourceIds; }

    public List<Long> getRelatedResourceIds() { return relatedResourceIds; }
    public void setRelatedResourceIds(List<Long> relatedResourceIds) { this.relatedResourceIds = relatedResourceIds; }
}