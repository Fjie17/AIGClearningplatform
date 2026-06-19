package com.example.learningplatform.dto;

import java.util.List;

public class AiRematchTestRequest {

    /** 可选：限定科目 */
    private Long subjectId;

    /** 可选：指定资源 ID，最多 5 条；不传则自动选取 */
    private List<Long> resourceIds;

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public List<Long> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<Long> resourceIds) {
        this.resourceIds = resourceIds;
    }
}
