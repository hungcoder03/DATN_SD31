package com.main.datn_sd31.config;

import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserControllerAdvice {
    
    @Autowired
    private NhanVienRepository nhanVienRepository;
    
    @ModelAttribute("currentUser")
    public NhanVien getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && 
            authentication.isAuthenticated() && 
            !authentication.getPrincipal().equals("anonymousUser")) {
            return nhanVienRepository.findByEmail(authentication.getName())
                .orElse(null);
        }
        return null;
    }
}
