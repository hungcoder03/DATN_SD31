package com.main.datn_sd31.util;

import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("get_nhan_vien")
@RequiredArgsConstructor
public class GetNhanVien {

    private final NhanVienRepository nhanVienRepository;

    public NhanVien getCurrentNhanVien() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với email: " + email));
    }

}
