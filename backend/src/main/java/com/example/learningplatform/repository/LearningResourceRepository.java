package com.example.learningplatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.learningplatform.entity.LearningResource;

@Repository
public interface LearningResourceRepository extends JpaRepository<LearningResource, Long> {
    Page<LearningResource> findBySubjectId(Long subjectId, Pageable pageable);
    Page<LearningResource> findByKnowledgePointId(Long knowledgePointId, Pageable pageable);
    List<LearningResource> findByPlatform(String platform);
    
    @Query("SELECT r FROM LearningResource r WHERE " +
           "(?1 IS NULL OR r.subjectId = ?1) AND " +
           "(?2 IS NULL OR r.knowledgePointId = ?2) AND " +
           "(?3 IS NULL OR r.platform = ?3)")
    Page<LearningResource> searchResources(Long subjectId, Long knowledgePointId, 
                                           String platform, Pageable pageable);
    
    boolean existsByResourceUrl(String resourceUrl);
    
    Optional<LearningResource> findByResourceUrl(String resourceUrl);

    List<LearningResource> findByIdIn(List<Long> ids);

    @Query("SELECT COUNT(r) FROM LearningResource r WHERE r.aiMatchVersion IS NULL OR r.aiMatchVersion <> :currentVersion")
    long countOutdatedByVersion(String currentVersion);

    @Query("SELECT COUNT(r) FROM LearningResource r WHERE (r.aiMatchVersion IS NULL OR r.aiMatchVersion <> :currentVersion) "
            + "AND (:subjectId IS NULL OR r.subjectId = :subjectId)")
    long countOutdatedByVersionAndSubjectId(String currentVersion, Long subjectId);

    @Query("SELECT r FROM LearningResource r WHERE (r.aiMatchVersion IS NULL OR r.aiMatchVersion <> :currentVersion) "
            + "AND (:subjectId IS NULL OR r.subjectId = :subjectId) ORDER BY r.id ASC")
    List<LearningResource> findOutdatedByVersion(String currentVersion, Long subjectId);

    @Query("SELECT r.aiMatchVersion, COUNT(r) FROM LearningResource r GROUP BY r.aiMatchVersion ORDER BY r.aiMatchVersion")
    List<Object[]> countGroupByAiMatchVersion();
}