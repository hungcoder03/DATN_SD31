package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.service.KhachHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class KhachHangServiceImpl implements KhachHangService {

    private final KhachHangRepository khachHangRepository;

    @Override
    public KhachHang findByEmail(String email) {
        return khachHangRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với email: " + email));
    }

    @Override
    public void save(KhachHang khachHang) {
        khachHangRepository.save(khachHang);
    }

    @Override
    public void capNhatEmail(Integer id, String emailMoi) {
        KhachHang kh = khachHangRepository.find(id);
        kh.setEmail(emailMoi);
        khachHangRepository.save(kh);
    }

    @Override
    public void capNhatSoDienThoai(Integer id, String soDienThoaiMoi) {
        KhachHang kh = khachHangRepository.find(id);
        kh.setSoDienThoai(soDienThoaiMoi);
        khachHangRepository.save(kh);
    }

    @Override
    public void capNhatDiaChi(Integer id, String diaChiMoi) {
        KhachHang kh = khachHangRepository.find(id);
        kh.setDiaChi(diaChiMoi);
        khachHangRepository.save(kh);
    }
    
    @Override
    public KhachHang dangKyKhachHang(KhachHang khachHang, String xacNhanMatKhau) {
        // Kiểm tra mật khẩu xác nhận
        if (!khachHang.getMatKhau().equals(xacNhanMatKhau)) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp!");
        }
        
        // Kiểm tra email đã tồn tại
        if (emailDaTonTai(khachHang.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }
        
        // Tạo mã khách hàng tự động
        khachHang.setMa(taoMaKhachHang());
        
        // Mã hóa mật khẩu
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        khachHang.setMatKhau(passwordEncoder.encode(khachHang.getMatKhau()));
        
        // Set các giá trị mặc định
        khachHang.setNgayThamGia(LocalDateTime.now());
        khachHang.setNgayTao(LocalDateTime.now());
        khachHang.setTrangThai(true);
        
        // Lưu khách hàng
        return khachHangRepository.save(khachHang);
    }
    
    @Override
    public boolean emailDaTonTai(String email) {
        return khachHangRepository.findByEmail(email).isPresent();
    }
    
    @Override
    public String taoMaKhachHang() {
        Random random = new Random();
        StringBuilder ma = new StringBuilder("KH");
        
        // Thêm 6 số ngẫu nhiên
        for (int i = 0; i < 6; i++) {
            ma.append(random.nextInt(10));
        }
        
        return ma.toString();
    }
}
