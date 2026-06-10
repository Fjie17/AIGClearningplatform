package com.example.learningplatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.learningplatform.entity.UserSubject;

@Repository
public interface UserSubjectRepository extends JpaRepository<UserSubject, Long> {
    
	Optional<UserSubject> findByUserIdAndSubjectId(Long userId, Long subjectId);
    
	List<UserSubject> findByUserId(Long userId);
}
