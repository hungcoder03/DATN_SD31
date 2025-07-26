package com.main.datn_sd31.controller.admin_controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@Controller
public class AdminController {
    @GetMapping({"/admin", "/admin/dashboard"})
    public String showDashBoard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("role", auth.getAuthorities());
        return "admin/pages/dashboard";
    }
}
