package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.GioHangChiTiet;
import com.main.datn_sd31.service.ChiTietSanPhamService;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChiTietSanPhamServiceImpl implements ChiTietSanPhamService {

    @Autowired
    private Chitietsanphamrepository chitietsanphamRepository;

    @Override
    public boolean kiemTraTonKho(List<GioHangChiTiet> gioHangChiTiets) {
        for (GioHangChiTiet item : gioHangChiTiets) {
            ChiTietSanPham ctsp = item.getChiTietSp();
            if (item.getSoLuong() > ctsp.getSoLuong()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean xoaBienThe(Integer bienTheId) {
        try {
            // Kiểm tra có thể xóa không
            if (!coTheXoaBienThe(bienTheId)) {
                return false;
            }
            
            // Backup dữ liệu trước khi xóa (có thể mở rộng sau)
            backupBienThe(bienTheId);
            
            // Thực hiện xóa
            chitietsanphamRepository.deleteById(bienTheId);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Optional<ChiTietSanPham> findById(Integer id) {
        return chitietsanphamRepository.findById(id);
    }

    @Override
    public boolean coTheXoaBienThe(Integer bienTheId) {
        // Kiểm tra có trong đơn hàng không
        boolean coTrongDonHang = chitietsanphamRepository.existsInHoaDonChiTiet(bienTheId);
        if (coTrongDonHang) {
            return false;
        }
        
        // Kiểm tra có trong giỏ hàng không
        boolean coTrongGioHang = chitietsanphamRepository.existsInGioHangChiTiet(bienTheId);
        if (coTrongGioHang) {
            return false;
        }
        
        return true;
    }

    @Override
    public Optional<ChiTietSanPham> findByIdWithDetails(Integer id) {
        return chitietsanphamRepository.findByIdWithDetails(id);
    }
    
    private void backupBienThe(Integer bienTheId) {
        // TODO: Implement backup logic
        // Có thể lưu vào bảng backup hoặc log
        System.out.println("Backup biến thể ID: " + bienTheId + " tại: " + LocalDateTime.now());
    }
}
