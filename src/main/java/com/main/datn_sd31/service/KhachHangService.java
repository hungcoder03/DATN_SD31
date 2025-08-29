package com.main.datn_sd31.service;

import com.main.datn_sd31.entity.KhachHang;

public interface KhachHangService {

    KhachHang findByEmail(String email);

    void save(KhachHang khachHang);

    void capNhatEmail(Integer id, String emailMoi);

    void capNhatSoDienThoai(Integer id, String soDienThoaiMoi);

    void capNhatDiaChi(Integer id, String diaChiMoi);
    
    // Method đăng ký khách hàng mới
    KhachHang dangKyKhachHang(KhachHang khachHang, String xacNhanMatKhau);
    
    // Kiểm tra email đã tồn tại
    boolean emailDaTonTai(String email);
    
    // Tạo mã khách hàng tự động
    String taoMaKhachHang();
}
