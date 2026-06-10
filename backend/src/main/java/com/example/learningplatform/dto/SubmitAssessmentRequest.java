package com.example.learningplatform.dto;

public class SubmitAssessmentRequest {

    private Long userId;
    private Long subjectId;
    private String Q1;
    private String Q2;
    private String Q3;
    private String Q4;

    public SubmitAssessmentRequest() {
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

    public String getQ1() {
        return Q1;
    }

    public void setQ1(String Q1) {
        this.Q1 = Q1;
    }

    public String getQ2() {
        return Q2;
    }

    public void setQ2(String Q2) {
        this.Q2 = Q2;
    }

    public String getQ3() {
        return Q3;
    }

    public void setQ3(String Q3) {
        this.Q3 = Q3;
    }

    public String getQ4() {
        return Q4;
    }

    public void setQ4(String Q4) {
        this.Q4 = Q4;
    }
}
