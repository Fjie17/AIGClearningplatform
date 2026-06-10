package com.example.learningplatform.service;

public interface UserSubjectService {

    boolean hasJoinedSubject(Long userId, Long subjectId);

    void joinSubject(Long userId, Long subjectId);

}
