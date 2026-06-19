package com.example.learningplatform.dto;

/**
 * 正式批量重新匹配请求：处理所有版本号不等于当前正式版的资源。
 */
public class AiRematchRequest {

    /** 可选：仅处理指定科目下的过期资源 */
    private Long subjectId;

    /**
     * 必须为 true 才会执行（防止误触全量 rematch）。
     * 可先调用 GET /ai-match-version 查看 outdatedCount。
     */
    private Boolean confirm;

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Boolean getConfirm() {
        return confirm;
    }

    public void setConfirm(Boolean confirm) {
        this.confirm = confirm;
    }
}
