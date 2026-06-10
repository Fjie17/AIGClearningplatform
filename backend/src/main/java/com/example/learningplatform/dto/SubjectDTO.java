package com.example.learningplatform.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Date;

public class SubjectDTO {
    private Long id;
    
    @NotBlank(message = "科目编码不能为空")
    private String code;
    
    @NotBlank(message = "科目名称不能为空")
    private String name;
    
    private String category;
    private String subCategory;
    private String description;
    private Integer status;
    private Date createdAt;

    // getter/setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}