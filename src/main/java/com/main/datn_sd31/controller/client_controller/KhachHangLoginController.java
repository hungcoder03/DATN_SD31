package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.KhachHangRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/khach-hang")
public class KhachHangLoginController {

    @GetMapping("/dang-nhap")
    public String hienThiFormLogin() {
        return "khachhang/login";  // trỏ tới file login.html trong templates/khachhang/
    }

}