package com.example.learningplatform.dto;

import java.util.ArrayList;
import java.util.List;

public class KnowledgePointSimpleDTO {

    private Long id;
    private String name;
    private List<KnowledgePointSimpleDTO> children = new ArrayList<>();

    public KnowledgePointSimpleDTO() {
    }

    public KnowledgePointSimpleDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<KnowledgePointSimpleDTO> getChildren() {
        return children;
    }

    public void setChildren(List<KnowledgePointSimpleDTO> children) {
        this.children = children;
    }
}
