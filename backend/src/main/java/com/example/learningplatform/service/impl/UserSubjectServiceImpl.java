package com.example.learningplatform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.learningplatform.repository.UserSubjectRepository;
import com.example.learningplatform.service.UserSubjectService;

@Service
public class UserSubjectServiceImpl implements UserSubjectService {

    @Autowired
    private UserSubjectRepository userSubjectRepository;

    @Override
    public boolean hasJoinedSubject(Long userId, Long subjectId) {
        return userSubjectRepository
                .findByUserIdAndSubjectId(userId, subjectId)
                .isPresent();
    }

    @Override
    public void joinSubject(Long userId, Long subjectId) {
        // TODO:
        // 1. 保存 user_subject
        // 2. 触发问卷（learning_profile）
        // 3. 后续生成 learning_path
    }
}
