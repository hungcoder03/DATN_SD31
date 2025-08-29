package com.main.datn_sd31.dto.san_pham_DTO;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamListDTO {
    private Integer id;
    private String ma;
    private String ten;
    private String moTa;
    private Boolean trangThai;
    private LocalDateTime ngayTao;
    
    // Thông tin phân loại
    private String chatLieu;
    private String danhMuc;
    private String kieuDang;
    private String thuongHieu;
    private String xuatXu;
    private String loaiThu;
    
    // Thông tin số lượng và giá
    private Integer tongSoLuong;
    private BigDecimal giaGocMin;
    private BigDecimal giaGocMax;
    private BigDecimal giaBanMin;
    private BigDecimal giaBanMax;
    private BigDecimal giaSauGiamMin;
    private BigDecimal giaSauGiamMax;
    
    // Thông tin giảm giá
    private String tenDotGiamGia;
    private BigDecimal phanTramGiam;
    private BigDecimal soTienGiam;
    
    // Trạng thái hiển thị
    private String trangThaiHienThi; // "Đang bán", "Ngưng bán", "Hết hàng", "Sắp hết"
    private String trangThaiClass; // CSS class cho styling
    
    // Ảnh sản phẩm
    private String anhChinh;
    private List<String> anhPhu;
    
    // Thông tin chi tiết
    private List<ChiTietSanPhamDTO> chiTietSanPhams;
    
    // Utility methods for formatting
    public String getGiaGocFormatted() {
        if (giaGocMin == null || giaGocMax == null) return "";
        if (giaGocMin.equals(giaGocMax)) {
            return String.format("%,.0fđ", giaGocMin);
        }
        return String.format("%,.0f - %,.0fđ", giaGocMin, giaGocMax);
    }
    
    public String getGiaSauGiamFormatted() {
        if (giaSauGiamMin == null || giaSauGiamMax == null) return "";
        if (giaSauGiamMin.equals(giaSauGiamMax)) {
            return String.format("%,.0fđ", giaSauGiamMin);
        }
        return String.format("%,.0f - %,.0fđ", giaSauGiamMin, giaSauGiamMax);
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChiTietSanPhamDTO {
        private Integer id;
        private String tenCt;
        private String size;
        private String mauSac;
        private Integer soLuong;
        private BigDecimal giaGoc;
        private BigDecimal giaBan;
        private BigDecimal giaSauGiam;
        private String trangThaiHienThi;
    }
} 