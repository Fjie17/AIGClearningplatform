package com.example.learningplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_profile",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "subject_id"}))
public class LearningProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "current_level")
    private Integer currentLevel;

    @Column(name = "learning_speed")
    private Integer learningSpeed;

    private String preference;

    @Column(name = "self_discipline")
    private Integer selfDiscipline;

    @Column(name = "LQAI")
    private String LQAI;

    @Column(name = "LQAI_code")
    private String LQAI_code;

    @Column(name = "profile")
    private String profile;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // getter / setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Integer getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(Integer currentLevel) { this.currentLevel = currentLevel; }

    public Integer getLearningSpeed() { return learningSpeed; }
    public void setLearningSpeed(Integer learningSpeed) { this.learningSpeed = learningSpeed; }

    public String getPreference() { return preference; }
    public void setPreference(String preference) { this.preference = preference; }

    public Integer getSelfDiscipline() { return selfDiscipline; }
    public void setSelfDiscipline(Integer selfDiscipline) { this.selfDiscipline = selfDiscipline; }

    public String getLQAI() { return LQAI; }
    public void setLQAI(String LQAI) { this.LQAI = LQAI; }

    public String getLQAI_code() { return LQAI_code; }
    public void setLQAI_code(String LQAI_code) { this.LQAI_code = LQAI_code; }

    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
