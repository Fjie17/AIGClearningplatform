package com.example.learningplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.learningplatform.entity.LearningBehaviorLog;

@Repository
public interface LearningBehaviorLogRepository extends JpaRepository<LearningBehaviorLog, Long> {
	
}
