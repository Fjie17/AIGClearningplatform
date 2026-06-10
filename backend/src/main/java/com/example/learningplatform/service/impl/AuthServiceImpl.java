package com.example.learningplatform.service.impl;

import com.example.learningplatform.dto.LoginRequest;
import com.example.learningplatform.dto.LoginResponse;
import com.example.learningplatform.dto.RegisterRequest;
import com.example.learningplatform.dto.ResetPasswordRequest;
import com.example.learningplatform.entity.User;
import com.example.learningplatform.repository.UserRepository;
import com.example.learningplatform.service.AuthService;
import com.example.learningplatform.service.EmailService;
import com.example.learningplatform.util.EmailCodeCache;
import com.example.learningplatform.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailCodeCache emailCodeCache;

    @Autowired
    private JwtUtil jwtUtil;
    
    //用户登录
    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("密码错误");

        // 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // 生成 JWT
        String token = jwtUtil.generateToken(user);

        LoginResponse resp = new LoginResponse();
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRole(user.getRole());
        resp.setToken(token);
        return resp;
    }
    
    // 用户注册
    @Override
    public String register(RegisterRequest request) {
        // 1. 校验两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) 
            return "两次密码不一致";

        // 2. 校验密码长度
        if (request.getPassword().length() < 8) 
            return "密码至少8位";

        // 3. 校验邮箱验证码
        if (!emailCodeCache.verifyCode(request.getEmail(), request.getEmailCode()))
            return "邮箱验证码错误或过期";

        // 4. 校验用户名和邮箱是否已注册
        if (userRepository.findByUsername(request.getUsername()).isPresent()) 
            return "用户名已存在";
        if (userRepository.findByEmail(request.getEmail()).isPresent()) 
            return "邮箱已注册";

        // 5. 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole("USER");           // 默认角色 USER
        user.setStatus(1);              // Integer 类型
        user.setEmailVerified(1);       // Integer 类型
        user.setCreatedAt(LocalDateTime.now());

        // 6. 保存用户到数据库
        userRepository.save(user);

        // 7. 移除已使用的验证码
        emailCodeCache.removeCode(request.getEmail());

        return "注册成功";
    }
    
    @Override
    public Map<String, Object> sendEmailCode(String email) {
        Map<String, Object> resp = new HashMap<>();
        try {
            String code = emailService.generateCode();
            emailService.sendEmail(email, code); // 真实发送邮件
            emailCodeCache.saveCode(email, code); // 缓存验证码5分钟

            resp.put("success", true);
            resp.put("message", "验证码已发送，有效期5分钟");
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            resp.put("message", "验证码发送失败: " + e.getMessage());
        }
        return resp;
    }
    
    @Override
    public String resetPassword(ResetPasswordRequest request) {
        // 验证密码一致性
        if (!request.getPassword().equals(request.getConfirmPassword()))
            return "两次密码不一致";
        if (request.getPassword().length() < 8)
            return "密码至少8位";

        // 验证验证码
        if (!emailCodeCache.verifyCode(request.getEmail(), request.getEmailCode()))
            return "邮箱验证码错误或过期";

        // 查找用户
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // 移除缓存
        emailCodeCache.removeCode(request.getEmail());

        return "密码修改成功";
    }
    
    @Override
    public Map<String, Object> getCurrentUser(String token) {
        Map<String, Object> resp = new HashMap<>();
        try {
            String username = jwtUtil.getUsername(token);
            if (username == null) {
                resp.put("success", false);
                resp.put("message", "无效的token");
                return resp;
            }
            
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            resp.put("success", true);
            resp.put("username", user.getUsername());
            resp.put("email", user.getEmail());
            resp.put("role", user.getRole());
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            resp.put("message", e.getMessage());
        }
        return resp;
    }

}
