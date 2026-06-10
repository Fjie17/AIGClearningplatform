package com.example.learningplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resource_match_profile")
public class ResourceMatchProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "level_min")
    private Integer levelMin;

    @Column(name = "level_max")
    private Integer levelMax;

    @Column(name = "speed_min")
    private Integer speedMin;

    @Column(name = "speed_max")
    private Integer speedMax;

    private String preference;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // getter / setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Integer getLevelMin() { return levelMin; }
    public void setLevelMin(Integer levelMin) { this.levelMin = levelMin; }

    public Integer getLevelMax() { return levelMax; }
    public void setLevelMax(Integer levelMax) { this.levelMax = levelMax; }

    public Integer getSpeedMin() { return speedMin; }
    public void setSpeedMin(Integer speedMin) { this.speedMin = speedMin; }

    public Integer getSpeedMax() { return speedMax; }
    public void setSpeedMax(Integer speedMax) { this.speedMax = speedMax; }

    public String getPreference() { return preference; }
    public void setPreference(String preference) { this.preference = preference; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
