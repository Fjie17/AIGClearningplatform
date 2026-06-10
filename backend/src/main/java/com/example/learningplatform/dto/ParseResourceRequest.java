package com.example.learningplatform.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class ParseResourceRequest {

    @NotBlank(message = "平台不能为空")
    private String platform;

    @NotBlank(message = "资源标题不能为空")
    private String title;

    @NotBlank(message = "资源链接不能为空")
    private String resourceUrl;

    private List<String> tags;

    private Long subjectId;

    private String description;

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getResourceUrl() { return resourceUrl; }
    public void setResourceUrl(String resourceUrl) { this.resourceUrl = resourceUrl; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}