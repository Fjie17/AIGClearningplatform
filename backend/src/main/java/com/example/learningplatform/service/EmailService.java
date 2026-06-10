package com.example.learningplatform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public String generateCode() {
        int code = (int)((Math.random() * 9 + 1) * 100000); // 6位验证码
        return String.valueOf(code);
    }

    public void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("2437657062@qq.com"); 
        message.setTo(to);
        message.setSubject("学习平台注册验证码");
        message.setText("您的验证码是：" + code + "，有效期5分钟");
        mailSender.send(message);
    }
    
    
}
