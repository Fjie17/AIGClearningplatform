package com.example.learningplatform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.learningplatform.entity.LearningPathItem;

@Repository
public interface LearningPathItemRepository extends JpaRepository<LearningPathItem, Long> {
    List<LearningPathItem> findByPathId(Long pathId);
}
