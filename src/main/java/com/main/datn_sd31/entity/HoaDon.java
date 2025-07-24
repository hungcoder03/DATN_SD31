package com.main.datn_sd31.entity;

import com.main.datn_sd31.Enum.TrangThaiHoaDon;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "hoa_don")
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "ma", nullable = false, length = 50)
    private String ma;

    @NotNull
    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;

    @NotNull
    @ColumnDefault("getdate()")
    @Column(name = "ngay_sua", nullable = false)
    private LocalDateTime ngaySua;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "khach_hang_id", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nhan_vien_id", nullable = false)
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_giam_gia_id")
    private PhieuGiamGia phieuGiamGia;

    @NotNull
    @Column(name = "ngay_thanh_toan", nullable = false)
    private LocalDateTime ngayThanhToan;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "phuong_thuc", nullable = false, length = 100)
    private String phuongThuc;

    @NotNull
    @Column(name = "gia_goc", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaGoc;

    @NotNull
    @Column(name = "gia_giam_gia", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaGiamGia;

    @NotNull
    @Column(name = "thanh_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal thanhTien;

    @Column(name = "loai_hoa_don", nullable = false)
    private String loaihoadon;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ten_nguoi_nhan", nullable = false, length = 100)
    private String tenNguoiNhan;

    @Size(max = 20)
    @Nationalized
    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Size(max = 100)
    @Nationalized
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 255)
    @Nationalized
    @Column(name = "dia_chi")
    private String diaChi;

    @Nationalized
    @Lob
    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "nguoi_tao")
    private Integer nguoiTao;

    @Column(name = "nguoi_sua")
    private Integer nguoiSua;

    @NotNull
    @Column(name = "phi_van_chuyen", nullable = false, precision = 18, scale = 2)
    private BigDecimal phiVanChuyen;

    @OneToMany(mappedBy = "hoaDon")
    private Set<HoaDonChiTiet> hoaDonChiTiets = new LinkedHashSet<>();

    @OneToMany(mappedBy = "hoaDon")
    private Set<LichSuHoaDon> lichSuHoaDons = new LinkedHashSet<>();

    @Transient
    public String getTrangThaiMoTa() {
        TrangThaiHoaDon enumTrangThai = TrangThaiHoaDon.fromValue(this.trangThai);
        return (enumTrangThai != null) ? enumTrangThai.getMoTa() : "Không xác định";
    }

}