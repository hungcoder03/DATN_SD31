package com.main.datn_sd31.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "dot_giam_gia")
public class DotGiamGia {
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
    @NotNull
    @Nationalized
    @Column(name = "ten", nullable = false, length = 100)
    private String ten;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @NotNull
    @Column(name = "gia_tri_dot_giam_gia", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaTriDotGiamGia;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @Column(name = "nguoi_sua")
    private Integer nguoiSua;

    @Column(name = "nguoi_tao")
    private Integer nguoiTao;

    @Size(max = 50)
    @Nationalized
    @Column(name = "loai", length = 50)
    private String loai;

//    @OneToMany(mappedBy = "dotGiamGia")
//    private Set<ChiTietSanPham> chiTietSanPhams = new LinkedHashSet<>();

    @OneToMany(mappedBy = "dotGiamGia")
    private List<ChiTietSanPham> sanPhams;

}