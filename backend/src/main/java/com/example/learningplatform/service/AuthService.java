package com.example.learningplatform.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.learningplatform.dto.LoginRequest;
import com.example.learningplatform.dto.LoginResponse;
import com.example.learningplatform.dto.RegisterRequest;
import com.example.learningplatform.dto.ResetPasswordRequest;

@Service
public interface AuthService {
	
    LoginResponse login(LoginRequest request);
    
    Map<String, Object> sendEmailCode(String email);

    String register(RegisterRequest request);
    
    String resetPassword(ResetPasswordRequest request);
    
    Map<String, Object> getCurrentUser(String token);
}
