package com.main.datn_sd31.controller.admin_controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/thong-ke")
public class ThongKeController {

    @GetMapping("")
    public String hoaDon(Model model) {
        model.addAttribute("page", "admin/pages/thong-ke");
        return "admin/index";
    }

}
