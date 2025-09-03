package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.service.KhachHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class KhachHangServiceImpl implements KhachHangService {

    private final KhachHangRepository khachHangRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
    public void capNhatTen(Integer id, String ten) {
        KhachHang kh = khachHangRepository.find(id);
        if (kh == null) {
            throw new RuntimeException("Không tìm thấy khách hàng!");
        }
        kh.setTen(ten);
        kh.setNgaySua(LocalDateTime.now());
        khachHangRepository.save(kh);
    }

    @Override
    public void capNhatEmail(Integer id, String emailMoi) {
        KhachHang kh = khachHangRepository.find(id);
        if (kh == null) {
            throw new RuntimeException("Không tìm thấy khách hàng!");
        }
        kh.setEmail(emailMoi);
        kh.setNgaySua(LocalDateTime.now());
        khachHangRepository.save(kh);
    }

    @Override
    public void capNhatSoDienThoai(Integer id, String soDienThoaiMoi) {
        KhachHang kh = khachHangRepository.find(id);
        if (kh == null) {
            throw new RuntimeException("Không tìm thấy khách hàng!");
        }
        kh.setSoDienThoai(soDienThoaiMoi);
        kh.setNgaySua(LocalDateTime.now());
        khachHangRepository.save(kh);
    }

    @Override
    public void capNhatGioiTinh(Integer id, Boolean gioiTinh) {
        KhachHang kh = khachHangRepository.find(id);
        if (kh == null) {
            throw new RuntimeException("Không tìm thấy khách hàng!");
        }
        kh.setGioiTinh(gioiTinh);
        kh.setNgaySua(LocalDateTime.now());
        khachHangRepository.save(kh);
    }

    @Override
    public void capNhatNgaySinh(Integer id, LocalDate ngaySinh) {
        KhachHang kh = khachHangRepository.find(id);
        if (kh == null) {
            throw new RuntimeException("Không tìm thấy khách hàng!");
        }
        kh.setNgaySinh(ngaySinh);
        kh.setNgaySua(LocalDateTime.now());
        khachHangRepository.save(kh);
    }

    @Override
    public void capNhatMatKhau(Integer id, String matKhauMoi) {
        KhachHang kh = khachHangRepository.find(id);
        if (kh == null) {
            throw new RuntimeException("Không tìm thấy khách hàng!");
        }
        kh.setMatKhau(passwordEncoder.encode(matKhauMoi));
        kh.setNgaySua(LocalDateTime.now());
        khachHangRepository.save(kh);
    }

    @Override
    public void capNhatDiaChi(Integer id, String diaChiMoi) {
        KhachHang kh = khachHangRepository.find(id);
        if (kh == null) {
            throw new RuntimeException("Không tìm thấy khách hàng!");
        }
        kh.setDiaChi(diaChiMoi);
        kh.setNgaySua(LocalDateTime.now());
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
    public boolean soDienThoaiDaTonTai(String soDienThoai) {
        return khachHangRepository.existsBySoDienThoai(soDienThoai);
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