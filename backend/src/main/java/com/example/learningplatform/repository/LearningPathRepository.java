package com.example.learningplatform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.learningplatform.entity.LearningPath;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {
    List<LearningPath> findByUserId(Long userId);
}
