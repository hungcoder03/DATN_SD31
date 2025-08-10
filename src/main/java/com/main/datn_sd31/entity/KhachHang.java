package com.main.datn_sd31.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "khach_hang")
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "ma", nullable = false, length = 50)
    private String ma;

    @Size(max = 100)
    @Nationalized
    @Column(name = "ten", length = 100)
    private String ten;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Size(max = 20)
    @Nationalized
    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "ngay_tham_gia")
    private LocalDateTime ngayThamGia;

    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;

    @Size(max = 100)
    @Nationalized
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 100)
    @Nationalized
    @Column(name = "mat_khau", length = 100)
    private String matKhau;

    @Nationalized
    @Lob
    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "nguoi_tao")
    private Integer nguoiTao;

    @Column(name = "nguoi_sua")
    private Integer nguoiSua;

    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @OneToMany(mappedBy = "khachHang")
    private Set<DanhGia> danhGias = new LinkedHashSet<>();

    @OneToMany(mappedBy = "khachHang")
    private Set<GioHangChiTiet> gioHangChiTiets = new LinkedHashSet<>();

    @OneToMany(mappedBy = "khachHang")
    private Set<HoaDon> hoaDons = new LinkedHashSet<>();

    @OneToMany(mappedBy = "khachHang")
    private Set<SpYeuThich> spYeuThiches = new LinkedHashSet<>();

}