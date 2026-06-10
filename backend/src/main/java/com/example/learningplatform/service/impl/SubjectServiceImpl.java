//package com.example.learningplatform.service.impl;
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.example.learningplatform.entity.Subject;
//import com.example.learningplatform.repository.SubjectRepository;
//import com.example.learningplatform.service.SubjectService;
//
//@Service
//public class SubjectServiceImpl implements SubjectService {
//
//    @Autowired
//    private SubjectRepository subjectRepository;
//    
//    public SubjectServiceImpl(SubjectRepository subjectRepository) {
//        this.subjectRepository = subjectRepository;
//    }
//
//    @Override
//    public List<Subject> getAllActiveSubjects() {
//        return subjectRepository.findByStatus(1);
//    }
//
//    @Override
//    public Subject getSubjectDetail(Long subjectId) {
//        return subjectRepository.findById(subjectId).orElse(null);
//    }
//    
//    @Override
//    public Subject save(Subject subject) {
//        return subjectRepository.save(subject);
//    }
//
//    @Override
//    public List<Subject> findAll() {
//        return subjectRepository.findAll();
//    }
//}
