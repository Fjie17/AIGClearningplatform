//package com.example.learningplatform.controller;
//
//import java.util.List;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.example.learningplatform.dto.SubjectDTO;
//import com.example.learningplatform.entity.Subject;
//import com.example.learningplatform.service.SubjectService;
//
//@RestController
//@RequestMapping("/api/admin/subject")
//public class AdminSubjectController {
//
//    private final SubjectService subjectService;
//
//    public AdminSubjectController(SubjectService subjectService) {
//        this.subjectService = subjectService;
//    }
//
//    @PostMapping("/add")
//    public Subject addSubject(@RequestBody SubjectDTO dto) {
//        Subject subject = new Subject();
//        subject.setCode(dto.getcode());
//        subject.setName(dto.getname());
//        subject.setCategory(dto.getcategory());
//        subject.setDescription(dto.getdescription());
//        subject.setStatus(dto.getstatus());
//        return subjectService.save(subject);
//    }
//
//    @GetMapping("/list")
//    public List<Subject> list() {
//        return subjectService.findAll();
//    }
//}