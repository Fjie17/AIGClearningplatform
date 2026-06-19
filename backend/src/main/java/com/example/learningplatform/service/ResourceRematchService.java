package com.example.learningplatform.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.learningplatform.dto.AiMatchResult;
import com.example.learningplatform.dto.AiMatchVersionStatusDTO;
import com.example.learningplatform.dto.AiRematchItemResult;
import com.example.learningplatform.dto.AiRematchRequest;
import com.example.learningplatform.dto.AiRematchTestRequest;
import com.example.learningplatform.dto.AiRematchTestResponse;
import com.example.learningplatform.entity.KnowledgePoint;
import com.example.learningplatform.entity.LearningResource;
import com.example.learningplatform.repository.KnowledgePointRepository;
import com.example.learningplatform.repository.LearningResourceRepository;

@Service
public class ResourceRematchService {

    private static final Logger logger = Logger.getLogger(ResourceRematchService.class.getName());

    @Autowired
    private LearningResourceRepository resourceRepository;

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    @Autowired
    private AiMatchService aiMatchService;

    @Autowired
    private AiMatchVersionManager versionManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 版本概览：当前正式版、测试版、各版本资源数量、待升级数量。
     */
    public AiMatchVersionStatusDTO getVersionStatus(Long subjectId) {
        String currentVersion = versionManager.getCurrentVersion();

        AiMatchVersionStatusDTO status = new AiMatchVersionStatusDTO();
        status.setCurrentVersion(currentVersion);
        status.setTestVersion(versionManager.buildTestVersion());

        long outdatedCount = subjectId == null
                ? resourceRepository.countOutdatedByVersion(currentVersion)
                : resourceRepository.countOutdatedByVersionAndSubjectId(currentVersion, subjectId);
        status.setOutdatedCount(outdatedCount);

        List<AiMatchVersionStatusDTO.VersionStat> stats = new ArrayList<>();
        long total = 0;
        long currentCount = 0;

        for (Object[] row : resourceRepository.countGroupByAiMatchVersion()) {
            String version = row[0] != null ? row[0].toString() : null;
            long count = row[1] != null ? ((Number) row[1]).longValue() : 0;
            stats.add(new AiMatchVersionStatusDTO.VersionStat(version, count));
            total += count;
            if (versionManager.isCurrentVersion(version)) {
                currentCount += count;
            }
        }

        status.setTotalResources(total);
        status.setCurrentVersionCount(currentCount);
        status.setVersionStats(stats);
        return status;
    }

    /**
     * 正式批量 rematch：所有版本号 ≠ 当前正式版的资源，重新 AI 匹配并写入当前正式版。
     * 每条资源独立提交，单条失败不影响其余。
     */
    public AiRematchTestResponse rematchOutdated(AiRematchRequest request) {
        String targetVersion = versionManager.getCurrentVersion();
        Long subjectId = request != null ? request.getSubjectId() : null;

        List<LearningResource> resources = resourceRepository.findOutdatedByVersion(targetVersion, subjectId);
        logger.info("开始正式批量 rematch，待处理 " + resources.size() + " 条，目标版本: " + targetVersion
                + (subjectId != null ? "，科目ID=" + subjectId : ""));

        return executeRematch(resources, targetVersion, "正式批量重新匹配");
    }

    /**
     * 开发测试：最多 5 条，写入「当前版本 + 测试版」。
     */
    public AiRematchTestResponse rematchTest(AiRematchTestRequest request) {
        String targetVersion = versionManager.buildTestVersion();
        List<LearningResource> resources = selectTestResources(request);

        logger.info("开始测试 rematch，共 " + resources.size() + " 条，目标版本: " + targetVersion);
        return executeRematch(resources, targetVersion, "测试重新匹配");
    }

    private AiRematchTestResponse executeRematch(List<LearningResource> resources, String targetVersion,
            String successMessage) {
        AiRematchTestResponse response = new AiRematchTestResponse();
        response.setBaseVersion(versionManager.getCurrentVersion());
        response.setAppliedVersion(targetVersion);
        response.setTotalSelected(resources.size());

        if (resources.isEmpty()) {
            response.getErrors().add("没有需要重新匹配的资源（均已为当前正式版 " + versionManager.getCurrentVersion() + "）");
            return response;
        }

        int index = 0;
        for (LearningResource resource : resources) {
            index++;
            AiRematchItemResult item = buildItemSnapshot(resource);

            try {
                transactionTemplate.executeWithoutResult(status -> rematchSingleResource(resource, targetVersion));
                item.setSuccess(true);
                item.setNewVersion(targetVersion);
                item.setNewKnowledgePointId(resource.getKnowledgePointId());
                item.setNewTags(resource.getTags());
                item.setNewConfidence(resource.getAiMatchConfidence());
                item.setMessage(successMessage + "成功");
                response.setSuccessCount(response.getSuccessCount() + 1);

                if (index % 10 == 0 || index == resources.size()) {
                    logger.info("rematch 进度: " + index + "/" + resources.size());
                }
            } catch (Exception e) {
                item.setSuccess(false);
                item.setMessage(e.getMessage());
                response.setFailedCount(response.getFailedCount() + 1);
                response.getErrors().add("资源ID=" + resource.getId() + ": " + e.getMessage());
                logger.warning("资源ID=" + resource.getId() + " rematch 失败: " + e.getMessage());
            }

            response.getResults().add(item);
        }

        return response;
    }

    private void rematchSingleResource(LearningResource resource, String targetVersion) {
        List<KnowledgePoint> knowledgePoints = knowledgePointRepository.findBySubjectId(resource.getSubjectId());
        if (knowledgePoints.isEmpty()) {
            throw new RuntimeException("科目 subjectId=" + resource.getSubjectId() + " 下无知识点");
        }

        AiMatchResult aiResult = aiMatchService.processResourceWithKnowledgePoints(
                resource.getTitle(),
                resource.getTopic(),
                resource.getResourceUrl(),
                resource.getPlatform(),
                knowledgePoints,
                targetVersion);

        applyMatchResult(resource, aiResult);
        resourceRepository.save(resource);
    }

    private AiRematchItemResult buildItemSnapshot(LearningResource resource) {
        AiRematchItemResult item = new AiRematchItemResult();
        item.setResourceId(resource.getId());
        item.setTitle(resource.getTitle());
        item.setResourceUrl(resource.getResourceUrl());
        item.setPreviousVersion(resource.getAiMatchVersion());
        item.setPreviousKnowledgePointId(resource.getKnowledgePointId());
        item.setPreviousTags(resource.getTags());
        item.setPreviousConfidence(resource.getAiMatchConfidence());
        return item;
    }

    private List<LearningResource> selectTestResources(AiRematchTestRequest request) {
        if (request.getResourceIds() != null && !request.getResourceIds().isEmpty()) {
            List<Long> ids = request.getResourceIds().stream()
                    .distinct()
                    .sorted()
                    .limit(AiMatchVersionManager.TEST_BATCH_LIMIT)
                    .toList();
            List<LearningResource> resources = resourceRepository.findByIdIn(ids);
            resources.sort(Comparator.comparing(LearningResource::getId));
            return resources;
        }

        PageRequest page = PageRequest.of(0, AiMatchVersionManager.TEST_BATCH_LIMIT, Sort.by("id").ascending());
        if (request.getSubjectId() != null) {
            return resourceRepository.findBySubjectId(request.getSubjectId(), page).getContent();
        }
        return resourceRepository.findAll(page).getContent();
    }

    private void applyMatchResult(LearningResource resource, AiMatchResult aiResult) {
        resource.setKnowledgePointId(aiResult.getKnowledgePointId());
        resource.setAiMatchConfidence(aiResult.getConfidence());
        resource.setAiMatchInfo(aiResult.getMatchInfo());
        resource.setTags(aiResult.getTags());
        resource.setDescription(aiResult.getDescription());
        resource.setAiMatchVersion(aiResult.getAiMatchVersion());
    }
}
