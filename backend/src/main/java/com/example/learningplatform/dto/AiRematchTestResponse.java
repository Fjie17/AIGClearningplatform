package com.example.learningplatform.dto;

import java.util.ArrayList;
import java.util.List;

public class AiRematchTestResponse {

    private int totalSelected;
    private int successCount;
    private int failedCount;
    private String appliedVersion;
    private String baseVersion;
    private List<AiRematchItemResult> results = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public int getTotalSelected() {
        return totalSelected;
    }

    public void setTotalSelected(int totalSelected) {
        this.totalSelected = totalSelected;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public String getAppliedVersion() {
        return appliedVersion;
    }

    public void setAppliedVersion(String appliedVersion) {
        this.appliedVersion = appliedVersion;
    }

    public String getBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(String baseVersion) {
        this.baseVersion = baseVersion;
    }

    public List<AiRematchItemResult> getResults() {
        return results;
    }

    public void setResults(List<AiRematchItemResult> results) {
        this.results = results;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
