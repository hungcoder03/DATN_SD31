package com.main.datn_sd31.controller.admin_controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
    @GetMapping("/admin")
    public String showDashBoard(){
        return "admin/pages/dashboard";
    }
}
