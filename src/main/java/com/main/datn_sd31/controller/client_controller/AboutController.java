package com.main.datn_sd31.controller.client_controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/about")
public class AboutController {

    @GetMapping("")
    public String about(Model model) {
        // Dữ liệu mẫu cho trang About
        model.addAttribute("companyName", "about.company.name");
        model.addAttribute("companySlogan", "about.company.slogan");
        model.addAttribute("companyDescription", "about.company.description");
        model.addAttribute("companyHistory", "about.company.history");
        model.addAttribute("companyMission", "about.company.mission");
        model.addAttribute("companyVision", "about.company.vision");
        model.addAttribute("companyValues", "about.company.values");
        
        // Thông tin thành lập
        model.addAttribute("foundedYear", "2020");
        model.addAttribute("foundedLocation", "about.company.founded_location");
        
        // Số liệu thống kê
        model.addAttribute("stats", Arrays.asList(
            new Stat("about.stats.customers", "10,000+"),
            new Stat("about.stats.products", "500+"),
            new Stat("about.stats.experience", "4+"),
            new Stat("about.stats.satisfaction", "98%")
        ));
        
        // Đội ngũ nhân viên
        model.addAttribute("teamMembers", Arrays.asList(
            new TeamMember("about.team.ceo.name", "about.team.ceo.position", "about.team.ceo.description"),
            new TeamMember("about.team.manager.name", "about.team.manager.position", "about.team.manager.description"),
            new TeamMember("about.team.vet.name", "about.team.vet.position", "about.team.vet.description")
        ));
        
        model.addAttribute("activePage", "about");
        return "client/pages/about/about";
    }

    // Inner class cho thống kê
    public static class Stat {
        private String labelKey;
        private String value;

        public Stat(String labelKey, String value) {
            this.labelKey = labelKey;
            this.value = value;
        }

        public String getLabelKey() { return labelKey; }
        public String getValue() { return value; }
    }

    // Inner class cho thành viên đội ngũ
    public static class TeamMember {
        private String nameKey;
        private String positionKey;
        private String descriptionKey;

        public TeamMember(String nameKey, String positionKey, String descriptionKey) {
            this.nameKey = nameKey;
            this.positionKey = positionKey;
            this.descriptionKey = descriptionKey;
        }

        public String getNameKey() { return nameKey; }
        public String getPositionKey() { return positionKey; }
        public String getDescriptionKey() { return descriptionKey; }
    }
} 