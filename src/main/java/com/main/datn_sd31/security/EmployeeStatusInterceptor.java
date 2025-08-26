package com.main.datn_sd31.security;

import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class EmployeeStatusInterceptor implements HandlerInterceptor {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // Chỉ kiểm tra cho các request admin
        if (request.getRequestURI().startsWith("/admin/")) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                
                // Kiểm tra nếu user là nhân viên
                if (authenticationService.isEmployee()) {
                    NhanVien nhanVien = nhanVienRepository.findByEmail(authentication.getName()).orElse(null);
                    
                    // Nếu nhân viên bị khóa, logout và redirect
                    if (nhanVien != null && !nhanVien.getTrangThai()) {
                        // Invalidate session
                        HttpSession session = request.getSession(false);
                        if (session != null) {
                            session.invalidate();
                        }
                        
                        // Clear security context
                        SecurityContextHolder.clearContext();
                        
                        // Redirect to login page with error message
                        response.sendRedirect("/admin/dang-nhap?error=account_disabled");
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
} 