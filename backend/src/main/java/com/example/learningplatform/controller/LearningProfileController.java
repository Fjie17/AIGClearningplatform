package com.example.learningplatform.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.learningplatform.dto.ApiResponse;
import com.example.learningplatform.dto.LQAIDetermineResponse;
import com.example.learningplatform.dto.LQAIRequest;
import com.example.learningplatform.dto.LearningProfileDTO;
import com.example.learningplatform.dto.SubjectAssessmentQuestionDTO;
import com.example.learningplatform.dto.SubmitAssessmentRequest;
import com.example.learningplatform.service.LearningProfileService;
import com.example.learningplatform.service.SubjectAssessmentQuestionService;

@RestController
@RequestMapping("/api/profile")
public class LearningProfileController {

    @Autowired
    private LearningProfileService learningProfileService;

    @Autowired
    private SubjectAssessmentQuestionService questionService;

    @PostMapping("/determine")
    public ResponseEntity<ApiResponse<LQAIDetermineResponse>> determineLQAI(@RequestBody LQAIRequest request) {
        try {
            LQAIDetermineResponse response = learningProfileService.determineLQAI(request.getUserId(), request.getSubjectId());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage(), LQAIDetermineResponse.class));
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<LQAIDetermineResponse>> submitAssessment(@RequestBody SubmitAssessmentRequest request) {
        try {
            LQAIDetermineResponse response = learningProfileService.submitAssessment(request);
            ApiResponse<LQAIDetermineResponse> apiResponse = new ApiResponse<>();
            apiResponse.setCode(200);
            apiResponse.setMessage("提交成功");
            apiResponse.setData(response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            ApiResponse<LQAIDetermineResponse> errorResponse = new ApiResponse<>();
            errorResponse.setCode(500);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * 获取指定科目的测试题列表
     */
    @GetMapping("/assessment-questions")
    public ResponseEntity<ApiResponse<List<SubjectAssessmentQuestionDTO>>> getAssessmentQuestions(
            @RequestParam Long subjectId) {
        try {
            List<SubjectAssessmentQuestionDTO> questions = questionService.listAll(subjectId);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (RuntimeException e) {
            ApiResponse<List<SubjectAssessmentQuestionDTO>> errorResponse = new ApiResponse<>();
            errorResponse.setCode(500);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * 获取用户在指定科目的学习画像
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<LearningProfileDTO>> getProfile(
            @RequestParam Long userId,
            @RequestParam Long subjectId) {
        try {
            LearningProfileDTO profile = learningProfileService.getProfile(userId, subjectId);
            if (profile != null) {
                return ResponseEntity.ok(ApiResponse.success(profile));
            } else {
                return ResponseEntity.ok(ApiResponse.error("学习画像不存在", LearningProfileDTO.class));
            }
        } catch (RuntimeException e) {
            ApiResponse<LearningProfileDTO> errorResponse = new ApiResponse<>();
            errorResponse.setCode(500);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
}