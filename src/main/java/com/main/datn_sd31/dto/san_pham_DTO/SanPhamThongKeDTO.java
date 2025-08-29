package com.main.datn_sd31.dto.san_pham_DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamThongKeDTO {
    private Long tongSanPham;
    private Long dangHoatDong;
    private Long ngungHoatDong;
    private Long sapHetHang;
} 