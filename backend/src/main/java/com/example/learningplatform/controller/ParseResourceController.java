package com.example.learningplatform.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.learningplatform.dto.AiMatchRequest;
import com.example.learningplatform.dto.AiMatchResult;
import com.example.learningplatform.dto.AiProcessResponse;
import com.example.learningplatform.dto.AiMatchVersionStatusDTO;
import com.example.learningplatform.dto.AiRematchRequest;
import com.example.learningplatform.dto.AiRematchTestRequest;
import com.example.learningplatform.dto.AiRematchTestResponse;
import com.example.learningplatform.dto.LearningResourceDTO;
import com.example.learningplatform.dto.ParsePreviewResponse;
import com.example.learningplatform.dto.ResourceStructureDTO;
import com.example.learningplatform.dto.Result;
import com.example.learningplatform.dto.StructureStatsDTO;
import com.example.learningplatform.entity.KnowledgePoint;
import com.example.learningplatform.repository.KnowledgePointRepository;
import com.example.learningplatform.service.AiMatchService;
import com.example.learningplatform.service.AiMatchVersionManager;
import com.example.learningplatform.service.ParseResourceService;
import com.example.learningplatform.service.ResourceRematchService;
import com.example.learningplatform.service.StructureService;

@RestController
@RequestMapping("/api/admin/resources")
public class ParseResourceController {

    @Autowired
    private ParseResourceService parseResourceService;

    @Autowired
    private AiMatchService aiMatchService;

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    @Autowired
    private StructureService structureService;

    @Autowired
    private ResourceRematchService resourceRematchService;

    /**
     * 清洗数据接口
     * 接收Excel/CSV文件和必要参数，清洗数据后返回预览结果
     */
    @PostMapping("/clean")
    public Result<ParsePreviewResponse> cleanData(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subjectId") Long subjectId,
            @RequestParam("platform") String platform,
            @RequestParam("resourceType") String resourceType) {
        
        try {
            ParsePreviewResponse response = parseResourceService.cleanData(file, subjectId, platform, resourceType);
            return Result.success("清洗成功", response);
        } catch (Exception e) {
            return Result.error("清洗失败: " + e.getMessage());
        }
    }

    /**
     * AI处理接口
     * 调用AI生成知识点匹配、标签、置信度、描述等信息，然后保存到数据库
     */
    @PostMapping("/ai-process")
    public Result<AiProcessResponse> processWithAi(
            @RequestParam("cacheKey") String cacheKey,
            @RequestParam("subjectId") Long subjectId) {
        
        try {
            AiProcessResponse response = parseResourceService.processWithAi(cacheKey, subjectId);
            return Result.success("AI处理完成", response);
        } catch (Exception e) {
            return Result.error("AI处理失败: " + e.getMessage());
        }
    }

    /**
     * 单条资源AI匹配（辅助接口）
     * 根据资源标题和介绍，调用AI从指定科目的知识点列表中匹配最合适的知识点
     */
    @PostMapping("/ai-match")
    public Result<AiMatchResult> aiMatch(@RequestBody AiMatchRequest request) {
        try {
            if (request.getSubjectId() == null) {
                return Result.error("请提供科目ID (subjectId)");
            }
            if (request.getTitle() == null || request.getTitle().isEmpty()) {
                return Result.error("请提供资源标题 (title)");
            }
            
            List<KnowledgePoint> knowledgePoints = knowledgePointRepository.findBySubjectId(request.getSubjectId());
            
            if (knowledgePoints == null || knowledgePoints.isEmpty()) {
                return Result.error("该科目下没有知识点数据");
            }
            
            AiMatchResult result = aiMatchService.processResourceWithKnowledgePoints(
                request.getTitle(),
                request.getTopic(),
                request.getResourceUrl(),
                request.getPlatform(),
                knowledgePoints
            );
            
            return Result.success("匹配成功", result);
        } catch (Exception e) {
            return Result.error("匹配失败: " + e.getMessage());
        }
    }

    /**
     * 重新 AI 匹配（开发测试版）
     * 基于资源 URL 页面内容重新匹配，最多处理 5 条以节省 token；
     * 写入版本号为「当前算法版本 + 测试版」（如 1.0测试版）。
     */
    @PostMapping("/ai-rematch-test")
    public Result<AiRematchTestResponse> rematchTest(@RequestBody(required = false) AiRematchTestRequest request) {
        try {
            if (request == null) {
                request = new AiRematchTestRequest();
            }
            if (request.getResourceIds() != null && request.getResourceIds().size() > AiMatchVersionManager.TEST_BATCH_LIMIT) {
                return Result.error("测试模式最多指定 " + AiMatchVersionManager.TEST_BATCH_LIMIT + " 条资源");
            }
            AiRematchTestResponse response = resourceRematchService.rematchTest(request);
            return Result.success("测试重新匹配完成", response);
        } catch (Exception e) {
            return Result.error("测试重新匹配失败: " + e.getMessage());
        }
    }

    /**
     * 查看 AI 匹配版本状态（各版本资源数量、待升级数量）。
     */
    @GetMapping("/ai-match-version")
    public Result<AiMatchVersionStatusDTO> getAiMatchVersion(
            @RequestParam(required = false) Long subjectId) {
        try {
            return Result.success("查询成功", resourceRematchService.getVersionStatus(subjectId));
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 正式批量重新 AI 匹配：处理所有 ai_match_version ≠ 当前正式版的资源，
     * 重新匹配后写入当前正式版（ai.match-version 配置值）。
     * 耗时可较长，建议先 GET /ai-match-version 确认 outdatedCount，再 confirm=true 执行。
     */
    @PostMapping("/ai-rematch")
    public Result<AiRematchTestResponse> rematchOutdated(@RequestBody AiRematchRequest request) {
        try {
            if (request == null || !Boolean.TRUE.equals(request.getConfirm())) {
                AiMatchVersionStatusDTO status = resourceRematchService.getVersionStatus(
                        request != null ? request.getSubjectId() : null);
                return Result.error(String.format(
                        "正式批量 rematch 需 confirm=true。当前正式版=%s，待升级=%d 条。请先 GET /ai-match-version 确认后再执行。",
                        status.getCurrentVersion(), status.getOutdatedCount()));
            }
            AiRematchTestResponse response = resourceRematchService.rematchOutdated(request);
            return Result.success("正式批量重新匹配完成", response);
        } catch (Exception e) {
            return Result.error("正式批量重新匹配失败: " + e.getMessage());
        }
    }

    // ==================== 结构化处理子模块接口 ====================

    /**
     * 获取按知识点组织的资源结构
     * 将指定科目的资源按照知识点层级进行结构化组织
     */
    @GetMapping("/structure/{subjectId}")
    public Result<ResourceStructureDTO> getResourceStructure(@PathVariable Long subjectId) {
        try {
            ResourceStructureDTO result = structureService.getResourceStructure(subjectId);
            return Result.success("获取成功", result);
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取知识点关联的资源列表
     * 获取指定知识点下的所有学习资源
     */
    @GetMapping("/structure/knowledge-point/{knowledgePointId}")
    public Result<List<LearningResourceDTO>> getResourcesByKnowledgePoint(@PathVariable Long knowledgePointId) {
        try {
            List<LearningResourceDTO> result = structureService.getResourcesByKnowledgePoint(knowledgePointId);
            return Result.success("获取成功", result);
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取结构化统计信息
     * 获取科目下知识点和资源的统计信息
     */
    @GetMapping("/structure/stats/{subjectId}")
    public Result<StructureStatsDTO> getStructureStats(@PathVariable Long subjectId) {
        try {
            StructureStatsDTO result = structureService.getStructureStats(subjectId);
            return Result.success("获取成功", result);
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }
}