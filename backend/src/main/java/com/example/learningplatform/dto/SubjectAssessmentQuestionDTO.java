package com.example.learningplatform.dto;

import com.example.learningplatform.entity.SubjectAssessmentQuestion;

import java.time.LocalDateTime;

public class SubjectAssessmentQuestionDTO {

    private Long id;
    private Long subjectId;
    private String questionCode;
    private String questionText;
    private SubjectAssessmentQuestion.QuestionType questionType;
    private SubjectAssessmentQuestion.MappingField mappingField;
    private String optionsJson;
    private Integer defaultOrder;
    private Integer isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getQuestionCode() {
        return questionCode;
    }

    public void setQuestionCode(String questionCode) {
        this.questionCode = questionCode;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public SubjectAssessmentQuestion.QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(SubjectAssessmentQuestion.QuestionType questionType) {
        this.questionType = questionType;
    }

    public SubjectAssessmentQuestion.MappingField getMappingField() {
        return mappingField;
    }

    public void setMappingField(SubjectAssessmentQuestion.MappingField mappingField) {
        this.mappingField = mappingField;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }

    public Integer getDefaultOrder() {
        return defaultOrder;
    }

    public void setDefaultOrder(Integer defaultOrder) {
        this.defaultOrder = defaultOrder;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
