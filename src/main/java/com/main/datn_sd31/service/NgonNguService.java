package com.main.datn_sd31.service;

import com.main.datn_sd31.entity.NgonNgu;
import com.main.datn_sd31.repository.NgonNguRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NgonNguService {
    
    private final NgonNguRepository ngonNguRepository;
    
    /**
     * Lấy tất cả ngôn ngữ
     */
    public List<NgonNgu> layTatCaNgonNgu() {
        return ngonNguRepository.findAll();
    }
    
    /**
     * Lấy danh sách ngôn ngữ hoạt động
     */
    public List<NgonNgu> layDanhSachNgonNguHoatDong() {
        return ngonNguRepository.findByTrangThaiTrueOrderByThuTu();
    }
    
    /**
     * Lấy ngôn ngữ theo mã
     */
    public Optional<NgonNgu> layNgonNguTheoMa(String maNgonNgu) {
        return ngonNguRepository.findByMaNgonNgu(maNgonNgu);
    }
    
    /**
     * Lấy ngôn ngữ mặc định
     */
    public Optional<NgonNgu> layNgonNguMacDinh() {
        return ngonNguRepository.findByMacDinhTrue();
    }
    
    /**
     * Lưu ngôn ngữ mới
     */
    public NgonNgu luuNgonNgu(NgonNgu ngonNgu) {
        // Nếu đặt làm mặc định, bỏ mặc định của ngôn ngữ khác
        if (ngonNgu.getMacDinh()) {
            ngonNguRepository.findByMacDinhTrue().ifPresent(ng -> {
                ng.setMacDinh(false);
                ngonNguRepository.save(ng);
            });
        }
        
        return ngonNguRepository.save(ngonNgu);
    }
    
    /**
     * Cập nhật ngôn ngữ
     */
    public NgonNgu capNhatNgonNgu(NgonNgu ngonNgu) {
        // Nếu đặt làm mặc định, bỏ mặc định của ngôn ngữ khác
        if (ngonNgu.getMacDinh()) {
            ngonNguRepository.findByMacDinhTrue().ifPresent(ng -> {
                if (!ng.getId().equals(ngonNgu.getId())) {
                    ng.setMacDinh(false);
                    ngonNguRepository.save(ng);
                }
            });
        }
        
        return ngonNguRepository.save(ngonNgu);
    }
    
    /**
     * Xóa ngôn ngữ
     */
    public void xoaNgonNgu(Integer id) {
        NgonNgu ngonNgu = ngonNguRepository.findById(id).orElse(null);
        if (ngonNgu != null && ngonNgu.getMacDinh()) {
            throw new RuntimeException("Không thể xóa ngôn ngữ mặc định!");
        }
        ngonNguRepository.deleteById(id);
    }
    
    /**
     * Đổi trạng thái ngôn ngữ
     */
    public NgonNgu doiTrangThaiNgonNgu(Integer id) {
        NgonNgu ngonNgu = ngonNguRepository.findById(id).orElse(null);
        if (ngonNgu != null) {
            if (ngonNgu.getMacDinh() && ngonNgu.getTrangThai()) {
                throw new RuntimeException("Không thể tắt ngôn ngữ mặc định!");
            }
            ngonNgu.setTrangThai(!ngonNgu.getTrangThai());
            return ngonNguRepository.save(ngonNgu);
        }
        return null;
    }
    
    /**
     * Kiểm tra mã ngôn ngữ có tồn tại không
     */
    public boolean kiemTraMaNgonNguTonTai(String maNgonNgu) {
        return ngonNguRepository.existsByMaNgonNgu(maNgonNgu);
    }
    
    /**
     * Lấy ngôn ngữ theo ID
     */
    public Optional<NgonNgu> layNgonNguTheoId(Integer id) {
        return ngonNguRepository.findById(id);
    }
}
