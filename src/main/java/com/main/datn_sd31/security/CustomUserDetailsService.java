package com.main.datn_sd31.security;

import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Tìm nhân viên
        NhanVien nv = nhanVienRepository.findByEmail(email).orElse(null);
        if (nv != null) {
            String role = "ROLE_NHANVIEN";
            if (nv.getChucVu() != null) {
                if (nv.getChucVu().equalsIgnoreCase("Quản lý") || nv.getChucVu().equalsIgnoreCase("ADMIN")) {
                    role = "ROLE_ADMIN";
                } else if (nv.getChucVu().equalsIgnoreCase("Nhân viên") || nv.getChucVu().equalsIgnoreCase("NHANVIEN")) {
                    role = "ROLE_NHANVIEN";
                }
            }
            return buildUser(nv.getEmail(), nv.getMatKhau(), role);
        }

        // Tìm khách hàng
        KhachHang kh = khachHangRepository.findByEmail(email).orElse(null);
        if (kh != null) {
            return buildUser(kh.getEmail(), kh.getMatKhau(), "ROLE_KHACHHANG");
        }

        throw new UsernameNotFoundException("Không tìm thấy tài khoản");
    }

    private UserDetails buildUser(String email, String password, String role) {
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(email)
                .password(password)
                .authorities(new SimpleGrantedAuthority(role))
                .build();
    }
}