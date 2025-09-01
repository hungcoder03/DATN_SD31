package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.AITrainingData;
import com.main.datn_sd31.service.GeminiAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/ai-training")
@RequiredArgsConstructor
public class AITrainingDataController {

    private final GeminiAIService geminiAIService;

    @GetMapping
    public String trainingDataPage(Model model) {
        try {
            List<AITrainingData> trainingData = geminiAIService.getAllTrainingData();
            List<String> categories = geminiAIService.getAllCategories();
            
            model.addAttribute("trainingData", trainingData);
            model.addAttribute("categories", categories);
            model.addAttribute("newTrainingData", new AITrainingData());
            
            return "admin/pages/ai-training";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải dữ liệu: " + e.getMessage());
            return "admin/pages/ai-training";
        }
    }

    @PostMapping("/save")
    public String saveTrainingData(@ModelAttribute AITrainingData trainingData, 
                                 RedirectAttributes redirectAttributes) {
        try {
            if (trainingData.getId() == null) {
                trainingData.setCreatedAt(LocalDateTime.now());
            }
            trainingData.setUpdatedAt(LocalDateTime.now());
            trainingData.setIsActive(true);
            
            geminiAIService.saveTrainingData(trainingData);
            redirectAttributes.addFlashAttribute("success", "Lưu dữ liệu training thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu dữ liệu: " + e.getMessage());
        }
        
        return "redirect:/admin/ai-training";
    }

    @PostMapping("/delete/{id}")
    public String deleteTrainingData(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            geminiAIService.deleteTrainingData(id);
            redirectAttributes.addFlashAttribute("success", "Xóa dữ liệu training thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa dữ liệu: " + e.getMessage());
        }
        
        return "redirect:/admin/ai-training";
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public AITrainingData getTrainingData(@PathVariable Long id) {
        // Trả về JSON để edit form
        return geminiAIService.getAllTrainingData().stream()
            .filter(td -> td.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
} 