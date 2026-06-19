package com.example.learningplatform.dto;

/**
 * 从资源 URL 抓取到的页面实况，供 AI 匹配时作为首要依据。
 */
public class ResourcePageContent {

    private boolean fetchSuccess;
    private String fetchSource;
    private String resourceUrl;
    private String pageTitle;
    private String pageDescription;
    private String author;
    private String pageTags;
    private String bodySummary;
    private String errorMessage;

    public boolean isFetchSuccess() {
        return fetchSuccess;
    }

    public void setFetchSuccess(boolean fetchSuccess) {
        this.fetchSuccess = fetchSuccess;
    }

    public String getFetchSource() {
        return fetchSource;
    }

    public void setFetchSource(String fetchSource) {
        this.fetchSource = fetchSource;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getPageDescription() {
        return pageDescription;
    }

    public void setPageDescription(String pageDescription) {
        this.pageDescription = pageDescription;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPageTags() {
        return pageTags;
    }

    public void setPageTags(String pageTags) {
        this.pageTags = pageTags;
    }

    public String getBodySummary() {
        return bodySummary;
    }

    public void setBodySummary(String bodySummary) {
        this.bodySummary = bodySummary;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String toPromptSection() {
        StringBuilder sb = new StringBuilder();
        sb.append("- 资源链接：").append(resourceUrl != null ? resourceUrl : "无").append("\n");
        sb.append("- 抓取状态：").append(fetchSuccess ? "成功" : "失败").append("\n");
        if (fetchSource != null) {
            sb.append("- 抓取方式：").append(fetchSource).append("\n");
        }
        if (!fetchSuccess && errorMessage != null) {
            sb.append("- 失败原因：").append(errorMessage).append("\n");
            return sb.toString();
        }
        sb.append("- 页面标题：").append(nullToEmpty(pageTitle)).append("\n");
        sb.append("- 页面简介：").append(nullToEmpty(pageDescription)).append("\n");
        sb.append("- 作者/UP主：").append(nullToEmpty(author)).append("\n");
        sb.append("- 页面标签：").append(nullToEmpty(pageTags)).append("\n");
        sb.append("- 正文摘要：").append(nullToEmpty(bodySummary));
        return sb.toString();
    }

    private String nullToEmpty(String value) {
        return value != null && !value.isBlank() ? value : "无";
    }
}
