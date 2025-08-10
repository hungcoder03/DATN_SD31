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

    @GetMapping("/admin/dang-xuat")
    public String logout(HttpSession session) {
        session.invalidate(); // Xoá session
        return "redirect:/admin/dang-nhap"; // Trả về trang login admin
    }
}