package com.example.learningplatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.learningplatform.entity.Subject;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {
    Optional<Subject> findByCode(String code);
    boolean existsByCode(String code);
    List<Subject> findByStatus(Integer status);
    
    List<Subject> findByNameContaining(String name);
}
