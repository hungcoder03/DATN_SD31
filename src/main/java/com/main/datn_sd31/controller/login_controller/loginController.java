//package com.example.demo.controller;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//@Controller
//public class loginController {
//
//    // Hiển thị form đăng nhập
//    @GetMapping("/login")
//    public String showLoginForm() {
//        return "login";
//    }
//
//    // Xử lý đăng nhập
//    @PostMapping("/login")
//    public String login(@RequestParam String username,
//                        @RequestParam String password,
//                        Model model,
//                        RedirectAttributes redirectAttributes) {
//        // Tài khoản cố định
//        if ("admin".equals(username) && "admin123".equals(password)) {
//            redirectAttributes.addFlashAttribute("success", "Đăng nhập thành công!");
//            return "redirect:/admin";
//        } else if ("nhanvien".equals(username) && "nv123".equals(password)) {
//            return "redirect:/nhanvien";
//        } else {
//            model.addAttribute("error", "Sai tài khoản hoặc mật khẩu!");
//            return "login";
//        }
//    }
//
//    // Giao diện admin
//    @GetMapping("/admin")
//    public String adminPage() {
//        return "adminhome.html";
//    }
//
//    // Giao diện nhân viên
//    @GetMapping("/nhanvien")
//    public String nhanvienPage() {
//        return "nhanvien";
//    }
//}