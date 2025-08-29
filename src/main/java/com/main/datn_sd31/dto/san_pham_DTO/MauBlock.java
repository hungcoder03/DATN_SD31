package com.main.datn_sd31.dto.san_pham_DTO;

import com.main.datn_sd31.entity.MauSac;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MauBlock {
    private MauSac mau;
    private boolean lightText; // true if background is light -> use dark text
    private int missingCount;
    private List<VariantRow> rows = new ArrayList<>();
    private ChiTietSanPhamForm form = new ChiTietSanPhamForm();
} 