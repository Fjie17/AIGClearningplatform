package com.example.learningplatform.service;

import com.example.learningplatform.dto.PageResult;
import com.example.learningplatform.dto.SubjectAssessmentQuestionDTO;
import com.example.learningplatform.entity.SubjectAssessmentQuestion;
import com.example.learningplatform.repository.SubjectAssessmentQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubjectAssessmentQuestionService {

    @Autowired
    private SubjectAssessmentQuestionRepository questionRepository;

    // ==================== 分页查询 ====================
    public PageResult<SubjectAssessmentQuestionDTO> page(int pageNum, int pageSize, Long subjectId, String questionCode) {
        // 构建动态查询条件
        Specification<SubjectAssessmentQuestion> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (subjectId != null) {
                predicates.add(cb.equal(root.get("subjectId"), subjectId));
            }

            if (questionCode != null && !questionCode.trim().isEmpty()) {
                predicates.add(cb.like(root.get("questionCode"), "%" + questionCode + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 分页并排序
        Page<SubjectAssessmentQuestion> page = questionRepository.findAll(spec,
                PageRequest.of(pageNum - 1, pageSize, Sort.by("defaultOrder").ascending()));

        List<SubjectAssessmentQuestionDTO> list = page.getContent().stream().map(this::toDTO).toList();
        return new PageResult<>(list, page.getTotalElements(), pageNum, pageSize);
    }

    // ==================== 查询所有 ====================
    public List<SubjectAssessmentQuestionDTO> listAll(Long subjectId) {
        List<SubjectAssessmentQuestion> list;
        if (subjectId != null) {
            list = questionRepository.findBySubjectIdOrderByDefaultOrderAsc(subjectId);
        } else {
            list = questionRepository.findAll(Sort.by("subjectId").ascending().and(Sort.by("defaultOrder").ascending()));
        }
        return list.stream().map(this::toDTO).toList();
    }

    // ==================== 根据ID查询 ====================
    public SubjectAssessmentQuestionDTO getById(Long id) {
        return questionRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    // ==================== 新增 ====================
    @Transactional
    public SubjectAssessmentQuestionDTO create(SubjectAssessmentQuestionDTO dto) {
        if (questionRepository.existsBySubjectIdAndQuestionCode(dto.getSubjectId(), dto.getQuestionCode())) {
            throw new RuntimeException("同一科目下题目编号已存在");
        }

        SubjectAssessmentQuestion question = new SubjectAssessmentQuestion();
        question.setSubjectId(dto.getSubjectId());
        question.setQuestionCode(dto.getQuestionCode());
        question.setQuestionText(dto.getQuestionText());
        question.setQuestionType(dto.getQuestionType());
        question.setMappingField(dto.getMappingField());
        question.setOptionsJson(dto.getOptionsJson());
        question.setDefaultOrder(dto.getDefaultOrder() != null ? dto.getDefaultOrder() : 0);
        question.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : 1);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());

        question = questionRepository.save(question);
        return toDTO(question);
    }

    // ==================== 修改 ====================
    @Transactional
    public SubjectAssessmentQuestionDTO update(Long id, SubjectAssessmentQuestionDTO dto) {
        SubjectAssessmentQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("题目不存在"));

        if (dto.getSubjectId() != null && !question.getSubjectId().equals(dto.getSubjectId())) {
            if (questionRepository.existsBySubjectIdAndQuestionCode(dto.getSubjectId(), dto.getQuestionCode())) {
                throw new RuntimeException("同一科目下题目编号已存在");
            }
            question.setSubjectId(dto.getSubjectId());
        } else if (dto.getQuestionCode() != null && !question.getQuestionCode().equals(dto.getQuestionCode())) {
            if (questionRepository.existsBySubjectIdAndQuestionCode(question.getSubjectId(), dto.getQuestionCode())) {
                throw new RuntimeException("同一科目下题目编号已存在");
            }
            question.setQuestionCode(dto.getQuestionCode());
        }

        if (dto.getQuestionCode() != null) question.setQuestionCode(dto.getQuestionCode());
        if (dto.getQuestionText() != null) question.setQuestionText(dto.getQuestionText());
        if (dto.getQuestionType() != null) question.setQuestionType(dto.getQuestionType());
        if (dto.getMappingField() != null) question.setMappingField(dto.getMappingField());
        if (dto.getOptionsJson() != null) question.setOptionsJson(dto.getOptionsJson());
        if (dto.getDefaultOrder() != null) question.setDefaultOrder(dto.getDefaultOrder());
        if (dto.getIsActive() != null) question.setIsActive(dto.getIsActive());
        question.setUpdatedAt(LocalDateTime.now());

        question = questionRepository.save(question);
        return toDTO(question);
    }

    // ==================== 删除 ====================
    @Transactional
    public void delete(Long id) {
        questionRepository.deleteById(id);
    }

    // ==================== 批量删除 ====================
    @Transactional
    public void deleteBatch(List<Long> ids) {
        questionRepository.deleteAllById(ids);
    }

    // ==================== DTO转换 ====================
    private SubjectAssessmentQuestionDTO toDTO(SubjectAssessmentQuestion question) {
        SubjectAssessmentQuestionDTO dto = new SubjectAssessmentQuestionDTO();
        dto.setId(question.getId());
        dto.setSubjectId(question.getSubjectId());
        dto.setQuestionCode(question.getQuestionCode());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setMappingField(question.getMappingField());
        dto.setOptionsJson(question.getOptionsJson());
        dto.setDefaultOrder(question.getDefaultOrder());
        dto.setIsActive(question.getIsActive());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getUpdatedAt());
        return dto;
    }
}
