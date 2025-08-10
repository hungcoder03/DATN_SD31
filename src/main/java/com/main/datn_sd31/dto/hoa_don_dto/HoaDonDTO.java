package com.main.datn_sd31.dto.hoa_don_dto;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonDTO {

    private String ma;

    private String maKH;

    private String tenKH;

    private String diaChi;

    private String email;

    private String soDienThoai;

    private String maNV;

    private String tenNhanVien;

    private TrangThaiLichSuHoaDon trangThaiLichSuHoaDon;

    private String phuongThuc;

    private String maGiamGia;

    private String giamGia;

    private BigDecimal giaGoc;

    private BigDecimal giaGiamGia;

    private BigDecimal phiVanChuyen;

    private BigDecimal thanhTien;

    private String ngayTao;

    private String capNhatLanCuoi;

    private String trangThaiHoaDonString;

    private Integer trangThaiHoaDonInteger;

    private String loaihoadon;

    private String ghiChu;

    private Integer lyDoGiaoKhongThanhCongEnum;

    public String getTrangThaiLichSuHoaDonMoTa() {
        return trangThaiLichSuHoaDon != null ? trangThaiLichSuHoaDon.getMoTa() : "Không rõ";
    }

    // Overload setter theo value
    public void setTrangThaiLichSuHoaDon(int value) {
        this.trangThaiLichSuHoaDon = TrangThaiLichSuHoaDon.fromValue(value);
    }


}