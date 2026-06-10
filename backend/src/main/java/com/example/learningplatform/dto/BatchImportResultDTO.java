package com.example.learningplatform.dto;

import java.util.List;

public class BatchImportResultDTO {
    private int total;
    private int success;
    private int fail;
    private List<String> errors;

    public BatchImportResultDTO() {}

    public BatchImportResultDTO(int total, int success, int fail, List<String> errors) {
        this.total = total;
        this.success = success;
        this.fail = fail;
        this.errors = errors;
    }

    // getter/setter
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getSuccess() { return success; }
    public void setSuccess(int success) { this.success = success; }
    public int getFail() { return fail; }
    public void setFail(int fail) { this.fail = fail; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}