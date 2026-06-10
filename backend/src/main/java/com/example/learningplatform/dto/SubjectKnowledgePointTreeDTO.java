package com.example.learningplatform.dto;

import java.util.ArrayList;
import java.util.List;

public class SubjectKnowledgePointTreeDTO {

    private Long subjectId;
    private String subjectName;
    private List<KnowledgePointSimpleDTO> children = new ArrayList<>();

    public SubjectKnowledgePointTreeDTO() {
    }

    public SubjectKnowledgePointTreeDTO(Long subjectId, String subjectName) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public List<KnowledgePointSimpleDTO> getChildren() {
        return children;
    }

    public void setChildren(List<KnowledgePointSimpleDTO> children) {
        this.children = children;
    }
}
