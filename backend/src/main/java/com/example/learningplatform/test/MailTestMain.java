package com.example.learningplatform.test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.time.LocalDateTime;
import java.util.Properties;

public class MailTestMain {

    public static void main(String[] args) {
        // 初始化 Spring 容器
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MailConfig.class);
        JavaMailSender mailSender = context.getBean(JavaMailSender.class);

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("2437657062@qq.com");   // 必须和配置一致
            msg.setTo("1919009624@qq.com");       // 可发给自己测试
            msg.setSubject("测试邮件");
            msg.setText("测试内容: " + LocalDateTime.now());
            mailSender.send(msg);
            System.out.println("邮件发送成功");
        } catch (Exception e) {
            e.printStackTrace();
        }

        context.close();
    }

    @Configuration
    static class MailConfig {
        @Bean
        public JavaMailSender javaMailSender() {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("smtp.qq.com");
            mailSender.setPort(465);
            mailSender.setUsername("2437657062@qq.com"); // SMTP 邮箱账号
            mailSender.setPassword("lnefgblrrvtldjbc");       // SMTP 授权码

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");  // 465端口必须启用SSL
            props.put("mail.debug", "true");            // 输出调试信息

            return mailSender;
        }
    }
}
