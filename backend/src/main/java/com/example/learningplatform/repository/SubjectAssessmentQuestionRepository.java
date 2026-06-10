package com.example.learningplatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.learningplatform.entity.SubjectAssessmentQuestion;

@Repository
public interface SubjectAssessmentQuestionRepository extends JpaRepository<SubjectAssessmentQuestion, Long>, JpaSpecificationExecutor<SubjectAssessmentQuestion> {
    Optional<SubjectAssessmentQuestion> findBySubjectIdAndQuestionCode(Long subjectId, String questionCode);
    boolean existsBySubjectIdAndQuestionCode(Long subjectId, String questionCode);
    List<SubjectAssessmentQuestion> findBySubjectId(Long subjectId);
    List<SubjectAssessmentQuestion> findBySubjectIdAndIsActive(Long subjectId, Integer isActive);
    List<SubjectAssessmentQuestion> findBySubjectIdOrderByDefaultOrderAsc(Long subjectId);
}
