
package com.main.datn_sd31.security;

import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CombinedUserDetailsService implements UserDetailsService {
    @Autowired
    private NhanVienRepository nhanVienRepository;
    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<NhanVien> nvOpt = nhanVienRepository.findByEmail(email);
        if (nvOpt.isPresent()) {
            NhanVien nv = nvOpt.get();
            String role = "ROLE_" + nv.getChucVu().toUpperCase();
            return new org.springframework.security.core.userdetails.User(
                    nv.getEmail(),
                    nv.getMatKhau(),
                    List.of(new SimpleGrantedAuthority(role))
            );
        }
        Optional<KhachHang> khOpt = khachHangRepository.findByEmail(email);
        if (khOpt.isPresent()) {
            KhachHang kh = khOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    kh.getEmail(),
                    kh.getMatKhau(),
                    List.of(new SimpleGrantedAuthority("ROLE_KHACHHANG"))
            );
        }
        throw new UsernameNotFoundException("Không tìm thấy tài khoản");
    }
}
