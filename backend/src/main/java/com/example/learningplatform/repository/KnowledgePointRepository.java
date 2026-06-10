package com.example.learningplatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.learningplatform.entity.KnowledgePoint;

@Repository
public interface KnowledgePointRepository extends JpaRepository<KnowledgePoint, Long>, 
                                                   JpaSpecificationExecutor<KnowledgePoint> {
    List<KnowledgePoint> findBySubjectId(Long subjectId);
    List<KnowledgePoint> findByParentId(Long parentId);
    List<KnowledgePoint> findBySubjectIdAndParentIdIsNull(Long subjectId);
    
    @Query("SELECT k FROM KnowledgePoint k WHERE k.subjectId = ?1 AND k.name = ?2")
    Optional<KnowledgePoint> findBySubjectIdAndName(Long subjectId, String name);
}