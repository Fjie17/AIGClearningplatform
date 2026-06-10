package com.example.learningplatform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "learning_path_item")
public class LearningPathItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "path_id")
    private Long pathId;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "knowledge_point_id")
    private Long knowledgePointId;

    @Column(name = "task_type")
    private String taskType;

    @Column(name = "task_content")
    private String taskContent;

    private String status;

    @Column(name = "order_no")
    private Integer orderNo;

    // getter / setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPathId() { return pathId; }
    public void setPathId(Long pathId) { this.pathId = pathId; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Long getKnowledgePointId() { return knowledgePointId; }
    public void setKnowledgePointId(Long knowledgePointId) { this.knowledgePointId = knowledgePointId; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public String getTaskContent() { return taskContent; }
    public void setTaskContent(String taskContent) { this.taskContent = taskContent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getOrderNo() { return orderNo; }
    public void setOrderNo(Integer orderNo) { this.orderNo = orderNo; }
}
