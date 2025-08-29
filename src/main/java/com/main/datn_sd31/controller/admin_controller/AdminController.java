package com.main.datn_sd31.controller.admin_controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
    @GetMapping("/admin")
    public String showDashBoard(){
        return "admin/pages/dashboard";
    }

    // Logout đã được xử lý trong AuthController
    // @GetMapping("/admin/dang-xuat") - REMOVED
}