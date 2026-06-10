package com.example.learningplatform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.learningplatform.dto.LoginRequest;
import com.example.learningplatform.dto.LoginResponse;
import com.example.learningplatform.dto.RegisterRequest;
import com.example.learningplatform.dto.ResetPasswordRequest;
import com.example.learningplatform.entity.User;
import com.example.learningplatform.service.AuthService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
    
    @PostMapping("/sendEmailCode")
    public Map<String, Object> sendEmailCode(@RequestBody Map<String,String> param) {
        String email = param.get("email");
        return authService.sendEmailCode(email);
    }

    @PostMapping("/regirster")
    public String register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }
    
    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }
    
    @GetMapping("/currentUser")
    public Map<String, Object> getCurrentUser(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        return authService.getCurrentUser(jwtToken);
    }
}
