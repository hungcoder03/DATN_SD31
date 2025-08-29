package com.main.datn_sd31.dto.san_pham_DTO;

import com.main.datn_sd31.entity.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariantRow {
    private Size size;
    private boolean existed;
    private BigDecimal giaGoc;
    private BigDecimal giaNhap;
    private BigDecimal giaBan;
    private Integer soLuong;
    private Integer formIndex; // index in ChiTietSanPhamForm for missing rows; null if existed
} 