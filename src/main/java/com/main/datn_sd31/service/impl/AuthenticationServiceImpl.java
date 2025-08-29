package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.main.datn_sd31.config.SecurityConstants.*;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;

    @Override
    public Map<String, Object> getCurrentUserInfo() {
        Map<String, Object> userInfo = new HashMap<>();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            
            userInfo.put("authenticated", true);
            userInfo.put("email", authentication.getName());
            
            // Check if user is customer
            KhachHang khachHang = getCurrentCustomer();
            if (khachHang != null) {
                userInfo.put("user", Map.of(
                    "id", khachHang.getId(),
                    "email", khachHang.getEmail(),
                    "ten", khachHang.getTen() != null ? khachHang.getTen() : "Khách hàng",
                    "soDienThoai", khachHang.getSoDienThoai() != null ? khachHang.getSoDienThoai() : ""
                ));
                userInfo.put("isCustomer", true);
            } else {
                // Check if user is employee
                NhanVien nhanVien = getCurrentEmployee();
                if (nhanVien != null) {
                    userInfo.put("user", Map.of(
                        "id", nhanVien.getId(),
                        "email", nhanVien.getEmail(),
                        "ten", nhanVien.getTen(),
                        "chucVu", nhanVien.getChucVu()
                    ));
                    userInfo.put("isEmployee", true);
                }
            }
            
        } else {
            userInfo.put("authenticated", false);
        }
        
        return userInfo;
    }

    @Override
    public KhachHang getCurrentCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && hasRole(ROLE_KHACHHANG)) {
            return khachHangRepository.findByEmail(authentication.getName()).orElse(null);
        }
        return null;
    }

    @Override
    public NhanVien getCurrentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && (hasRole(ROLE_ADMIN) || hasRole(ROLE_NHANVIEN))) {
            NhanVien nhanVien = nhanVienRepository.findByEmail(authentication.getName()).orElse(null);
            // Kiểm tra trạng thái hoạt động của nhân viên
            if (nhanVien != null && !nhanVien.getTrangThai()) {
                return null; // Trả về null nếu nhân viên bị khóa
            }
            return nhanVien;
        }
        return null;
    }

    @Override
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
        }
        return false;
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !authentication.getName().equals("anonymousUser");
    }

    @Override
    public boolean isCustomer() {
        return hasRole(ROLE_KHACHHANG);
    }

    @Override
    public boolean isAdmin() {
        return hasRole(ROLE_ADMIN);
    }

    @Override
    public boolean isEmployee() {
        return hasRole(ROLE_ADMIN) || hasRole(ROLE_NHANVIEN);
    }
} 