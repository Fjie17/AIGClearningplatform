package com.example.learningplatform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.learningplatform.config.AiConfig;

/**
 * AI 匹配算法版本统一管理。
 *
 * <p>版本约定：
 * <ul>
 *   <li>正式版：与 {@code ai.match-version} 配置一致，如 {@code 1.0}、{@code 2.0}</li>
 *   <li>测试版：正式版 + {@link #TEST_SUFFIX}，如 {@code 1.0测试版}，仅用于开发验证</li>
 *   <li>待升级：{@code ai_match_version} 为 null、不等于当前正式版、或为测试版</li>
 * </ul>
 */
@Component
public class AiMatchVersionManager {

    public static final String TEST_SUFFIX = "测试版";
    public static final int TEST_BATCH_LIMIT = 5;

    @Autowired
    private AiConfig aiConfig;

    /** 当前正式版版本号（来自配置 ai.match-version） */
    public String getCurrentVersion() {
        return aiConfig.getMatchVersion();
    }

    /** 当前配置对应的测试版版本号，如 1.0测试版 */
    public String buildTestVersion() {
        return getCurrentVersion() + TEST_SUFFIX;
    }

    /** 是否为当前正式版（精确匹配，测试版不算） */
    public boolean isCurrentVersion(String storedVersion) {
        if (storedVersion == null || storedVersion.isBlank()) {
            return false;
        }
        return getCurrentVersion().equals(storedVersion.trim());
    }

    /** 是否为测试版 */
    public boolean isTestVersion(String storedVersion) {
        return storedVersion != null && storedVersion.endsWith(TEST_SUFFIX);
    }

    /** 是否需要重新匹配并升级到当前正式版 */
    public boolean needsRematch(String storedVersion) {
        return !isCurrentVersion(storedVersion);
    }

    /** 去掉测试版后缀，得到基础版本号 */
    public String extractBaseVersion(String storedVersion) {
        if (storedVersion == null || storedVersion.isBlank()) {
            return null;
        }
        String trimmed = storedVersion.trim();
        if (trimmed.endsWith(TEST_SUFFIX)) {
            return trimmed.substring(0, trimmed.length() - TEST_SUFFIX.length());
        }
        return trimmed;
    }
}
