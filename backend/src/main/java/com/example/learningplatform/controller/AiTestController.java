package com.example.learningplatform.controller;

import com.example.learningplatform.config.AiConfig;
import com.example.learningplatform.service.AiMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/test")
@CrossOrigin(origins = "*")
public class AiTestController {

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private AiMatchService aiMatchService;

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getAiConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("apiUrl", aiConfig.getApiUrl());
        config.put("model", aiConfig.getModel());
        config.put("temperature", aiConfig.getTemperature());
        config.put("maxTokens", aiConfig.getMaxTokens());
        config.put("apiKey", aiConfig.getApiKey() != null && !aiConfig.getApiKey().isEmpty() ? "已配置" : "未配置");
        return ResponseEntity.ok(config);
    }

    @PostMapping("/simple-chat")
    public ResponseEntity<Map<String, Object>> testSimpleChat(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String message = request.get("message");
        
        if (message == null || message.isEmpty()) {
            response.put("success", false);
            response.put("error", "请提供message参数");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String aiResponse = aiMatchService.callAiApi(message);
            
            if (aiResponse != null && !aiResponse.isEmpty()) {
                response.put("success", true);
                response.put("response", aiResponse);
                response.put("message", "AI调用成功！");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "AI返回为空");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "调用失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    @PostMapping("/match-subject")
    public ResponseEntity<Map<String, Object>> testMatchSubject(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        String title = (String) request.get("title");
        
        if (title == null || title.isEmpty()) {
            response.put("success", false);
            response.put("error", "请提供title参数");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            List<String> tags = (List<String>) request.get("tags");
            Long subjectId = aiMatchService.matchSubject(title, tags);
            
            response.put("success", true);
            response.put("subjectId", subjectId);
            response.put("message", subjectId != null ? "匹配成功" : "未找到匹配的科目");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "调用失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "AI Service");
        return ResponseEntity.ok(response);
    }
}
