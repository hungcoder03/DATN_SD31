package com.main.datn_sd31.controller.client_controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/contact")
public class ContactController {

    @GetMapping("")
    public String contact(Model model) {
        // Thông tin liên hệ
        model.addAttribute("companyName", "contact.company.name");
        model.addAttribute("companyAddress", "contact.company.address");
        model.addAttribute("companyPhone", "contact.company.phone");
        model.addAttribute("companyEmail", "contact.company.email");
        model.addAttribute("companyWebsite", "contact.company.website");
        
        // Giờ làm việc
        model.addAttribute("workingHours", Arrays.asList(
            new WorkingHour("contact.hours.monday", "8:00 - 22:00"),
            new WorkingHour("contact.hours.tuesday", "8:00 - 22:00"),
            new WorkingHour("contact.hours.wednesday", "8:00 - 22:00"),
            new WorkingHour("contact.hours.thursday", "8:00 - 22:00"),
            new WorkingHour("contact.hours.friday", "8:00 - 22:00"),
            new WorkingHour("contact.hours.saturday", "8:00 - 22:00"),
            new WorkingHour("contact.hours.sunday", "8:00 - 22:00")
        ));
        
        // Dịch vụ hỗ trợ
        model.addAttribute("supportServices", Arrays.asList(
            new SupportService("contact.support.sales", "contact.support.sales.desc"),
            new SupportService("contact.support.technical", "contact.support.technical.desc"),
            new SupportService("contact.support.returns", "contact.support.returns.desc"),
            new SupportService("contact.support.emergency", "contact.support.emergency.desc")
        ));
        
        // Thông tin bổ sung
        model.addAttribute("additionalInfo", "contact.additional.info");
        model.addAttribute("mapLocation", "contact.map.location");
        
        model.addAttribute("activePage", "contact");
        return "client/pages/contact/contact";
    }

    @PostMapping("/send")
    public String sendContact(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam String subject,
                            @RequestParam String message,
                            RedirectAttributes redirectAttributes) {
        
        // TODO: Xử lý gửi email liên hệ (có thể tích hợp với email service sau)
        // Hiện tại chỉ hiển thị thông báo thành công
        
        redirectAttributes.addFlashAttribute("success", "contact.form.success");
        return "redirect:/contact";
    }

    // Inner class cho giờ làm việc
    public static class WorkingHour {
        private String dayKey;
        private String hours;

        public WorkingHour(String dayKey, String hours) {
            this.dayKey = dayKey;
            this.hours = hours;
        }

        public String getDayKey() { return dayKey; }
        public String getHours() { return hours; }
    }

    // Inner class cho dịch vụ hỗ trợ
    public static class SupportService {
        private String nameKey;
        private String descriptionKey;

        public SupportService(String nameKey, String descriptionKey) {
            this.nameKey = nameKey;
            this.descriptionKey = descriptionKey;
        }

        public String getNameKey() { return nameKey; }
        public String getDescriptionKey() { return descriptionKey; }
    }
} 