package com.main.datn_sd31.controller.admin_controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class KhuyenMaiController {

    @GetMapping("/khuyen-mai")
    public String khuyenMai(
            Model model
    ) {
        model.addAttribute("page", "admin/pages/khuyen-mai");
        return "admin/index";
    }

}
