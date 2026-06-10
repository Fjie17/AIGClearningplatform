package com.example.learningplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.learningplatform.entity.ResourceMatchProfile;

@Repository
public interface ResourceMatchProfileRepository extends JpaRepository<ResourceMatchProfile, Long> {
}
