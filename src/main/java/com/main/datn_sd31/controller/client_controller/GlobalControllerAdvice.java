package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.repository.KhachHangRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @ModelAttribute
    public void addKhachHangToSession(Authentication authentication, HttpSession session) {
        if (authentication != null && session.getAttribute("khachHang") == null) {
            String email = authentication.getName();
            khachHangRepository.findByEmail(email).ifPresent(khachHang -> {
                session.setAttribute("khachHang", khachHang);
            });
        }
    }
}