package com.example.learningplatform.dto;

import java.util.ArrayList;
import java.util.List;

public class AiMatchVersionStatusDTO {

    private String currentVersion;
    private String testVersion;
    private long totalResources;
    private long currentVersionCount;
    private long outdatedCount;
    private List<VersionStat> versionStats = new ArrayList<>();

    public static class VersionStat {
        private String version;
        private long count;

        public VersionStat() {
        }

        public VersionStat(String version, long count) {
            this.version = version;
            this.count = count;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getTestVersion() {
        return testVersion;
    }

    public void setTestVersion(String testVersion) {
        this.testVersion = testVersion;
    }

    public long getTotalResources() {
        return totalResources;
    }

    public void setTotalResources(long totalResources) {
        this.totalResources = totalResources;
    }

    public long getCurrentVersionCount() {
        return currentVersionCount;
    }

    public void setCurrentVersionCount(long currentVersionCount) {
        this.currentVersionCount = currentVersionCount;
    }

    public long getOutdatedCount() {
        return outdatedCount;
    }

    public void setOutdatedCount(long outdatedCount) {
        this.outdatedCount = outdatedCount;
    }

    public List<VersionStat> getVersionStats() {
        return versionStats;
    }

    public void setVersionStats(List<VersionStat> versionStats) {
        this.versionStats = versionStats;
    }
}
