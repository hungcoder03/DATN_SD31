package com.main.datn_sd31.service;

import com.main.datn_sd31.dto.thong_ke_dto.ThongKeSanPhamDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ThongKeService {

    BigDecimal getDoanhThu(LocalDateTime start, LocalDateTime end, Integer trangThai);

    Integer countDonHang(LocalDateTime start, LocalDateTime end, Integer trangThai);

    Integer getTongSanPham(LocalDateTime start, LocalDateTime end);

    List<ThongKeSanPhamDTO> getThongKeSanPham(LocalDateTime start, LocalDateTime end);
}
