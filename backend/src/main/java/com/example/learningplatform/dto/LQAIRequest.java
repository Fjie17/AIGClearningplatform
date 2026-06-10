package com.example.learningplatform.dto;

public class LQAIRequest {

    private Long userId;
    private Long subjectId;

    public LQAIRequest() {
    }

    public LQAIRequest(Long userId, Long subjectId) {
        this.userId = userId;
        this.subjectId = subjectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }
}