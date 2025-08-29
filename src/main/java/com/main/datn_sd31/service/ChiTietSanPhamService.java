package com.main.datn_sd31.service;

import com.main.datn_sd31.entity.GioHangChiTiet;
import com.main.datn_sd31.entity.ChiTietSanPham;

import java.util.List;
import java.util.Optional;

public interface ChiTietSanPhamService {

    boolean kiemTraTonKho(List<GioHangChiTiet> gioHangChiTiets);
    
    // Xóa biến thể sản phẩm
    boolean xoaBienThe(Integer bienTheId);
    
    // Tìm biến thể theo ID
    Optional<ChiTietSanPham> findById(Integer id);
    
    // Kiểm tra có thể xóa biến thể không
    boolean coTheXoaBienThe(Integer bienTheId);
    
    // Lấy thông tin biến thể với đầy đủ chi tiết
    Optional<ChiTietSanPham> findByIdWithDetails(Integer id);
}
