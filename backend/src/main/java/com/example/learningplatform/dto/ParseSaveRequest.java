package com.example.learningplatform.dto;

import java.util.List;

public class ParseSaveRequest {
    
    private List<SaveResourceDTO> resources;
    private boolean skipDuplicates = true;

    public List<SaveResourceDTO> getResources() { return resources; }
    public void setResources(List<SaveResourceDTO> resources) { this.resources = resources; }

    public boolean isSkipDuplicates() { return skipDuplicates; }
    public void setSkipDuplicates(boolean skipDuplicates) { this.skipDuplicates = skipDuplicates; }
}