package com.main.datn_sd31.dto.san_pham_DTO;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamFilterDTO {
    // Search parameters
    private String keyword;
    
    // Filter parameters
    private Integer danhMucId;
    private Integer loaiThuId;
    private Integer chatLieuId;
    private Integer kieuDangId;
    private Integer thuongHieuId;
    private Integer xuatXuId;
    private Boolean trangThai;
    private String trangThaiHienThi; // "Đang bán", "Ngưng bán", "Hết hàng", "Sắp hết"
    
    // Price range
    private BigDecimal giaMin;
    private BigDecimal giaMax;
    
    // Quantity range
    private Integer soLuongMin;
    private Integer soLuongMax;
    
    // Sort parameters
    private String sortBy; // "ten", "gia", "soLuong", "ngayTao", "trangThai"
    private String sortOrder; // "asc", "desc"
    
    // Pagination
    private Integer page;
    private Integer size;
    
    // Default values
    public static SanPhamFilterDTO getDefault() {
        return SanPhamFilterDTO.builder()
                .page(0)
                .size(10)
                .sortBy("ngayTao")
                .sortOrder("desc")
                .build();
    }
} 