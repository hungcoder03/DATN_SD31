package com.main.datn_sd31.controller;

import com.main.datn_sd31.dto.GeminiRequest;
import com.main.datn_sd31.entity.AITrainingData;
import com.main.datn_sd31.service.GeminiAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
public class AIChatController {

    private final GeminiAIService geminiAIService;

    @PostMapping("/chat")
    public ResponseEntity<?> chatWithAI(@RequestBody Map<String, Object> request) {
        try {
            String userMessage = (String) request.get("message");
            @SuppressWarnings("unchecked")
            List<String> conversationHistory = (List<String>) request.get("conversationHistory");
            
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tin nhắn không được để trống"));
            }
            
            String aiResponse = geminiAIService.generateResponse(userMessage, conversationHistory);
            
            return ResponseEntity.ok(Map.of(
                "response", aiResponse,
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/training-data")
    public ResponseEntity<List<AITrainingData>> getAllTrainingData() {
        try {
            List<AITrainingData> trainingData = geminiAIService.getAllTrainingData();
            return ResponseEntity.ok(trainingData);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        try {
            List<String> categories = geminiAIService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/training-data")
    public ResponseEntity<AITrainingData> saveTrainingData(@RequestBody AITrainingData trainingData) {
        try {
            AITrainingData saved = geminiAIService.saveTrainingData(trainingData);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/training-data/{id}")
    public ResponseEntity<?> deleteTrainingData(@PathVariable Long id) {
        try {
            geminiAIService.deleteTrainingData(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
} 