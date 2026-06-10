package com.example.learningplatform.dto;

import java.time.LocalDateTime;

public class LearningProfileDTO {

    private Long userId;
    private Long subjectId;
    private Integer currentLevel;
    private Integer learningSpeed;
    private String preference;
    private Integer selfDiscipline;
    private String lqai;
    private String lqaiCode;
    private String profile;
    private LocalDateTime updatedAt;

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

    public Integer getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(Integer currentLevel) {
        this.currentLevel = currentLevel;
    }

    public Integer getLearningSpeed() {
        return learningSpeed;
    }

    public void setLearningSpeed(Integer learningSpeed) {
        this.learningSpeed = learningSpeed;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    public Integer getSelfDiscipline() {
        return selfDiscipline;
    }

    public void setSelfDiscipline(Integer selfDiscipline) {
        this.selfDiscipline = selfDiscipline;
    }

    public String getLqai() {
        return lqai;
    }

    public void setLqai(String lqai) {
        this.lqai = lqai;
    }

    public String getLqaiCode() {
        return lqaiCode;
    }

    public void setLqaiCode(String lqaiCode) {
        this.lqaiCode = lqaiCode;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
