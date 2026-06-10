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
import com.example.learningplatform.dto.LearningResourceDTO;
import com.example.learningplatform.dto.ParsePreviewResponse;
import com.example.learningplatform.dto.ResourceStructureDTO;
import com.example.learningplatform.dto.Result;
import com.example.learningplatform.dto.StructureStatsDTO;
import com.example.learningplatform.entity.KnowledgePoint;
import com.example.learningplatform.repository.KnowledgePointRepository;
import com.example.learningplatform.service.AiMatchService;
import com.example.learningplatform.service.ParseResourceService;
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
                null,
                request.getPlatform(),
                knowledgePoints
            );
            
            return Result.success("匹配成功", result);
        } catch (Exception e) {
            return Result.error("匹配失败: " + e.getMessage());
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