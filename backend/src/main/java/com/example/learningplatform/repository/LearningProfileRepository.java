package com.example.learningplatform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.learningplatform.entity.LearningProfile;

@Repository
public interface LearningProfileRepository extends JpaRepository<LearningProfile, Long> {
    Optional<LearningProfile> findByUserIdAndSubjectId(Long userId, Long subjectId);
}
