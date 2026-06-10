package com.example.learningplatform.dto;

import java.util.List;

public class ParseSaveResponse {
    
    private int totalCount;
    private int successCount;
    private int failedCount;
    private int duplicateCount;
    private List<String> errors;

    public ParseSaveResponse() {
        this.errors = new java.util.ArrayList<>();
    }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }

    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

    public int getDuplicateCount() { return duplicateCount; }
    public void setDuplicateCount(int duplicateCount) { this.duplicateCount = duplicateCount; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}