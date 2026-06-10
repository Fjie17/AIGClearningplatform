package com.example.learningplatform.dto;

import java.util.List;

public class AiProcessResponse {
    private int successCount;
    private int failedCount;
    private List<String> errors;

    public AiProcessResponse() {
        this.errors = new java.util.ArrayList<>();
    }

    // getters and setters
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}