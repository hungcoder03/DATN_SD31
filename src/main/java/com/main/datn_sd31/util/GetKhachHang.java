package com.main.datn_sd31.util;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("get_khach_hang")
@RequiredArgsConstructor
public class GetKhachHang {

    private final KhachHangRepository khachHangRepository;

    public KhachHang getCurrentKhachHang() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Nếu chưa đăng nhập hoặc là anonymous user thì trả về null
        if (!authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        return khachHangRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với email: " + email));
    }
}
