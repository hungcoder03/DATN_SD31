package com.main.datn_sd31.dto.san_pham_DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamExportDTO {
    private String ma;
    private String ten;
    private String moTa;
    private String danhMuc;
    private String thuongHieu;
    private String chatLieu;
    private String xuatXu;
    private String kieuDang;
    private String loaiThu;
    private BigDecimal gia;
    private Integer soLuong;
    private String trangThai;
    private LocalDateTime ngayTao;
} 