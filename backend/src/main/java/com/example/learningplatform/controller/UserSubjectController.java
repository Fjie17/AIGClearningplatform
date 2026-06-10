package com.example.learningplatform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.learningplatform.dto.ApiResponse;
import com.example.learningplatform.service.UserSubjectService;

@RestController
@RequestMapping("/api/user-subject")
public class UserSubjectController {

    @Autowired
    private UserSubjectService userSubjectService;

    @PostMapping("/join")
    public ApiResponse<?> join(@RequestParam Long subjectId,
                               @AuthenticationPrincipal UserDetails user) {

        Long userId = Long.valueOf(user.getUsername()); // 示例
        userSubjectService.joinSubject(userId, subjectId);
        return ApiResponse.success();
    }
}
