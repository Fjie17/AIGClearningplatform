package com.example.learningplatform.service;

import com.example.learningplatform.dto.*;
import com.example.learningplatform.entity.KnowledgePoint;
import com.example.learningplatform.entity.LearningResource;
import com.example.learningplatform.entity.Subject;
import com.example.learningplatform.repository.KnowledgePointRepository;
import com.example.learningplatform.repository.LearningResourceRepository;
import com.example.learningplatform.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StructureService {

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    @Autowired
    private LearningResourceRepository learningResourceRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    public ResourceStructureDTO getResourceStructure(Long subjectId) {
        ResourceStructureDTO result = new ResourceStructureDTO();
        
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        if (subject != null) {
            result.setSubjectId(subjectId);
            result.setSubjectName(subject.getName());
        }

        List<KnowledgePoint> knowledgePoints = knowledgePointRepository.findBySubjectId(subjectId);
        List<LearningResource> resources = learningResourceRepository.findBySubjectId(subjectId, null).getContent();

        Map<Long, List<LearningResource>> resourcesByKp = resources.stream()
                .filter(r -> r.getKnowledgePointId() != null)
                .collect(Collectors.groupingBy(LearningResource::getKnowledgePointId));

        Map<Long, KnowledgePoint> kpMap = knowledgePoints.stream()
                .collect(Collectors.toMap(KnowledgePoint::getId, kp -> kp));

        List<ResourceStructureDTO.KnowledgePointNode> rootNodes = new ArrayList<>();
        
        for (KnowledgePoint kp : knowledgePoints) {
            if (kp.getParentId() == null) {
                ResourceStructureDTO.KnowledgePointNode node = buildNode(kp, kpMap, resourcesByKp);
                rootNodes.add(node);
            }
        }
        
        result.setKnowledgePoints(rootNodes);
        return result;
    }

    private ResourceStructureDTO.KnowledgePointNode buildNode(
            KnowledgePoint kp, 
            Map<Long, KnowledgePoint> kpMap, 
            Map<Long, List<LearningResource>> resourcesByKp) {
        
        ResourceStructureDTO.KnowledgePointNode node = new ResourceStructureDTO.KnowledgePointNode();
        node.setId(kp.getId());
        node.setName(kp.getName());
        node.setDifficulty(kp.getDifficulty());
        node.setExamWeight(kp.getExamWeight());
        node.setParentId(kp.getParentId());

        List<ResourceStructureDTO.ResourceSummary> resourceSummaries = new ArrayList<>();
        List<LearningResource> resources = resourcesByKp.get(kp.getId());
        if (resources != null) {
            for (LearningResource resource : resources) {
                ResourceStructureDTO.ResourceSummary summary = new ResourceStructureDTO.ResourceSummary();
                summary.setId(resource.getId());
                summary.setTitle(resource.getTitle());
                summary.setPlatform(resource.getPlatform());
                summary.setResourceType(resource.getResourceType());
                summary.setDuration(resource.getDuration());
                summary.setAiMatchConfidence(resource.getAiMatchConfidence());
                resourceSummaries.add(summary);
            }
        }
        node.setResources(resourceSummaries);

        List<ResourceStructureDTO.KnowledgePointNode> children = new ArrayList<>();
        for (KnowledgePoint child : kpMap.values()) {
            if (kp.getId().equals(child.getParentId())) {
                children.add(buildNode(child, kpMap, resourcesByKp));
            }
        }
        node.setChildren(children);

        return node;
    }

    public List<LearningResourceDTO> getResourcesByKnowledgePoint(Long knowledgePointId) {
        List<LearningResource> resources = learningResourceRepository.findByKnowledgePointId(knowledgePointId, null).getContent();
        return resources.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public StructureStatsDTO getStructureStats(Long subjectId) {
        StructureStatsDTO result = new StructureStatsDTO();
        
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        if (subject != null) {
            result.setSubjectId(subjectId);
            result.setSubjectName(subject.getName());
        }

        List<KnowledgePoint> knowledgePoints = knowledgePointRepository.findBySubjectId(subjectId);
        List<LearningResource> resources = learningResourceRepository.findBySubjectId(subjectId, null).getContent();

        result.setTotalKnowledgePoints(knowledgePoints.size());
        result.setTotalResources(resources.size());
        
        if (!knowledgePoints.isEmpty()) {
            result.setAvgResourcesPerKnowledgePoint(resources.size() / knowledgePoints.size());
        } else {
            result.setAvgResourcesPerKnowledgePoint(0);
        }

        Map<Long, Long> resourceCountByKp = resources.stream()
                .filter(r -> r.getKnowledgePointId() != null)
                .collect(Collectors.groupingBy(LearningResource::getKnowledgePointId, Collectors.counting()));

        List<StructureStatsDTO.KnowledgePointStats> statsList = new ArrayList<>();
        for (KnowledgePoint kp : knowledgePoints) {
            StructureStatsDTO.KnowledgePointStats stats = new StructureStatsDTO.KnowledgePointStats();
            stats.setKnowledgePointId(kp.getId());
            stats.setKnowledgePointName(kp.getName());
            stats.setResourceCount(resourceCountByKp.getOrDefault(kp.getId(), 0L).intValue());
            stats.setDifficulty(kp.getDifficulty());
            stats.setExamWeight(kp.getExamWeight());
            statsList.add(stats);
        }
        result.setKnowledgePointStats(statsList);

        return result;
    }

    private LearningResourceDTO toDTO(LearningResource resource) {
        LearningResourceDTO dto = new LearningResourceDTO();
        dto.setId(resource.getId());
        dto.setSubjectId(resource.getSubjectId());
        dto.setKnowledgePointId(resource.getKnowledgePointId());
        dto.setPlatform(resource.getPlatform());
        dto.setTitle(resource.getTitle());
        dto.setResourceUrl(resource.getResourceUrl());
        dto.setResourceType(resource.getResourceType());
        dto.setDuration(resource.getDuration());
        dto.setAuthor(resource.getAuthor());
        dto.setViews(resource.getViews());
        dto.setReviewCount(resource.getReviewCount());
        dto.setPublishTime(resource.getPublishTime());
        dto.setTopic(resource.getTopic());
        dto.setClassHours(resource.getClassHours());
        dto.setImageUrl(resource.getImageUrl());
        dto.setAiMatchConfidence(resource.getAiMatchConfidence());
        dto.setAiMatchInfo(resource.getAiMatchInfo());
        dto.setTags(resource.getTags());
        dto.setDescription(resource.getDescription());
        
        if (resource.getKnowledgePointId() != null) {
            knowledgePointRepository.findById(resource.getKnowledgePointId())
                    .ifPresent(kp -> dto.setKnowledgePointName(kp.getName()));
        }
        if (resource.getSubjectId() != null) {
            subjectRepository.findById(resource.getSubjectId())
                    .ifPresent(s -> dto.setSubjectName(s.getName()));
        }
        
        return dto;
    }
}