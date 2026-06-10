package com.example.learningplatform.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class KnowledgePointDTO {
    private Long id;
    
    @NotNull(message = "所属科目不能为空")
    private Long subjectId;
    
    private String subjectName;
    
    @NotNull(message = "知识点名称不能为空")
    private String name;
    
    private Integer difficulty;
    private Integer examWeight;
    private Long parentId;
    private String parentName;
    
    // 添加 children 字段用于树形结构
    private List<KnowledgePointDTO> children;

    // getter/setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
    public Integer getExamWeight() { return examWeight; }
    public void setExamWeight(Integer examWeight) { this.examWeight = examWeight; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public List<KnowledgePointDTO> getChildren() { return children; }
    public void setChildren(List<KnowledgePointDTO> children) { this.children = children; }
}