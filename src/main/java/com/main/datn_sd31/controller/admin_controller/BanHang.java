package com.main.datn_sd31.controller.admin_controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/ban-hang")
public class BanHang {

    @GetMapping("")
    public String dashboardAdmin(Model model) {
        model.addAttribute("page", "admin/pages/ban-hang");
        return "admin/index";
    }

}
