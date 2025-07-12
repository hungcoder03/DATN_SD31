package com.main.datn_sd31.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//Chạy file này để lấy mật khẩu mã hóa, sau đó cập nhật mật khẩu mã hóa vào DB
public class PasswordEncoderTool {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "100302h@";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Mật khẩu mã hóa: " + encodedPassword);
    }
}