package com.example.learningplatform.dto;

import java.util.List;

public class ParsePreviewResponse {
    
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private List<String> invalidReasons;
    private List<PreviewResourceDTO> previewData;
    private String cacheKey;

    public ParsePreviewResponse() {
        this.invalidReasons = new java.util.ArrayList<>();
        this.previewData = new java.util.ArrayList<>();
    }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getValidRows() { return validRows; }
    public void setValidRows(int validRows) { this.validRows = validRows; }

    public int getInvalidRows() { return invalidRows; }
    public void setInvalidRows(int invalidRows) { this.invalidRows = invalidRows; }

    public List<String> getInvalidReasons() { return invalidReasons; }
    public void setInvalidReasons(List<String> invalidReasons) { this.invalidReasons = invalidReasons; }

    public List<PreviewResourceDTO> getPreviewData() { return previewData; }
    public void setPreviewData(List<PreviewResourceDTO> previewData) { this.previewData = previewData; }

    public String getCacheKey() { return cacheKey; }
    public void setCacheKey(String cacheKey) { this.cacheKey = cacheKey; }
}