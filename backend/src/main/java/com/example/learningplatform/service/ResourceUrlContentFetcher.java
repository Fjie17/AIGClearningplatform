package com.example.learningplatform.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.learningplatform.dto.ResourcePageContent;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class ResourceUrlContentFetcher {

    private static final Logger logger = Logger.getLogger(ResourceUrlContentFetcher.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern BVID_PATTERN = Pattern.compile("(BV[a-zA-Z0-9]{10})");
    private static final int MAX_BODY_LENGTH = 3000;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.url-read-timeout-ms:60000}")
    private int readTimeoutMs;

    public ResourcePageContent fetch(String resourceUrl) {
        ResourcePageContent content = new ResourcePageContent();
        content.setResourceUrl(resourceUrl);

        if (resourceUrl == null || resourceUrl.isBlank()) {
            content.setFetchSuccess(false);
            content.setErrorMessage("资源链接为空");
            return content;
        }

        try {
            if (resourceUrl.contains("bilibili.com")) {
                return fetchBilibili(resourceUrl);
            }
            return fetchGenericHtml(resourceUrl);
        } catch (Exception e) {
            logger.warning("抓取资源链接失败: " + resourceUrl + ", " + e.getMessage());
            content.setFetchSuccess(false);
            content.setErrorMessage(e.getMessage());
            return content;
        }
    }

    private ResourcePageContent fetchBilibili(String resourceUrl) throws Exception {
        String bvid = extractBvid(resourceUrl);
        if (bvid == null) {
            return fetchGenericHtml(resourceUrl);
        }

        HttpHeaders headers = buildBrowserHeaders("https://www.bilibili.com");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String apiUrl = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, request, String.class);

        ResourcePageContent content = new ResourcePageContent();
        content.setResourceUrl(resourceUrl);
        content.setFetchSource("bilibili_api");

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            content.setFetchSuccess(false);
            content.setErrorMessage("B站 API 响应异常");
            return content;
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        if (root.path("code").asInt(-1) != 0) {
            content.setFetchSuccess(false);
            content.setErrorMessage("B站 API 返回错误: " + root.path("message").asText(""));
            return fetchGenericHtml(resourceUrl);
        }

        JsonNode data = root.path("data");
        content.setFetchSuccess(true);
        content.setPageTitle(data.path("title").asText(""));
        content.setPageDescription(data.path("desc").asText(""));
        content.setAuthor(data.path("owner").path("name").asText(""));

        int durationSec = data.path("duration").asInt(0);
        String durationText = durationSec > 0 ? "时长约" + (durationSec / 60) + "分钟" : "";
        String viewText = "播放量" + data.path("stat").path("view").asLong(0);
        content.setBodySummary(truncate(durationText + "，" + viewText + "。" + content.getPageDescription(), MAX_BODY_LENGTH));

        content.setPageTags(fetchBilibiliTags(bvid, headers));
        return content;
    }

    private String fetchBilibiliTags(String bvid, HttpHeaders headers) {
        try {
            String tagUrl = "https://api.bilibili.com/x/tag/archive/tags?bvid=" + bvid;
            ResponseEntity<String> response = restTemplate.exchange(tagUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "";
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.path("code").asInt(-1) != 0) {
                return "";
            }
            List<String> tags = new ArrayList<>();
            for (JsonNode tag : root.path("data")) {
                String name = tag.path("tag_name").asText("");
                if (!name.isBlank()) {
                    tags.add(name);
                }
            }
            return String.join(",", tags);
        } catch (Exception e) {
            return "";
        }
    }

    private ResourcePageContent fetchGenericHtml(String resourceUrl) throws Exception {
        Document doc = Jsoup.connect(resourceUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .referrer("https://www.google.com")
                .timeout(readTimeoutMs)
                .followRedirects(true)
                .get();

        ResourcePageContent content = new ResourcePageContent();
        content.setResourceUrl(resourceUrl);
        content.setFetchSource("html");
        content.setFetchSuccess(true);
        content.setPageTitle(doc.title());

        String description = firstNonBlank(
                doc.select("meta[name=description]").attr("content"),
                doc.select("meta[property=og:description]").attr("content"),
                doc.select("meta[name=Description]").attr("content"));
        content.setPageDescription(description);

        content.setAuthor(firstNonBlank(
                doc.select("meta[name=author]").attr("content"),
                doc.select("meta[property=og:site_name]").attr("content")));

        content.setPageTags(firstNonBlank(
                doc.select("meta[name=keywords]").attr("content"),
                doc.select("meta[property=og:tag]").attr("content")));

        String bodyText = doc.body() != null ? doc.body().text() : "";
        content.setBodySummary(truncate(bodyText, MAX_BODY_LENGTH));
        return content;
    }

    private HttpHeaders buildBrowserHeaders(String referer) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        headers.set("Referer", referer);
        return headers;
    }

    private String extractBvid(String url) {
        Matcher matcher = BVID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }
}
