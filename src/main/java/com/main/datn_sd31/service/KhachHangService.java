package com.main.datn_sd31.service;

import com.main.datn_sd31.entity.KhachHang;

import java.time.LocalDate;

public interface KhachHangService {

    // Existing methods
    KhachHang findByEmail(String email);
    void save(KhachHang khachHang);
    void capNhatEmail(Integer id, String emailMoi);
    void capNhatSoDienThoai(Integer id, String soDienThoaiMoi);
    void capNhatDiaChi(Integer id, String diaChiMoi);
    KhachHang dangKyKhachHang(KhachHang khachHang, String xacNhanMatKhau);
    boolean emailDaTonTai(String email);
    String taoMaKhachHang();

    // New methods
    void capNhatTen(Integer id, String ten);
    void capNhatGioiTinh(Integer id, Boolean gioiTinh);
    void capNhatNgaySinh(Integer id, LocalDate ngaySinh);
    void capNhatMatKhau(Integer id, String matKhauMoi);
    boolean soDienThoaiDaTonTai(String soDienThoai);
}