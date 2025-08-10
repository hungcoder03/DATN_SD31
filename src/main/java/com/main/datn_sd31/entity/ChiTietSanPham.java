package com.main.datn_sd31.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
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
@Table(name = "chi_tiet_san_pham")
public class ChiTietSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 200)
    @Nationalized
    @Column(name = "ten_ct", nullable = false, length = 200)
    private String tenCt;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "san_pham_id", nullable = false)
    private SanPham sanPham;

    @Column(name = "gia_goc")
    private BigDecimal giaGoc;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dot_giam_gia_id")
    private DotGiamGia dotGiamGia;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "size_id", nullable = false)
    private com.main.datn_sd31.entity.Size size;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "mau_sac_id", nullable = false)
    private MauSac mauSac;

    @Column(name = "gia_ban", precision = 18, scale = 2)
    private BigDecimal giaBan;

    @Column(name = "gia_nhap", precision = 18, scale = 2)
    private BigDecimal giaNhap;

    @Column(name = "ma_vach")
    private String maVach;

    @Nationalized
    @Lob
    @Column(name = "mo_ta")
    private String moTa;

    @NotNull
    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua;

    @Column(name = "nguoi_tao")
    private Integer nguoiTao;

    @Column(name = "nguoi_sua")
    private Integer nguoiSua;

    @Nationalized
    @Lob
    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @OneToMany(mappedBy = "chiTietSp")
    private Set<GioHangChiTiet> gioHangChiTiets = new LinkedHashSet<>();

    @OneToMany(mappedBy = "chiTietSanPham")
    private Set<HoaDonChiTiet> hoaDonChiTiets = new LinkedHashSet<>();

}