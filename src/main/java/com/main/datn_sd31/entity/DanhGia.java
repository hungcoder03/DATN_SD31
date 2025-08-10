package com.main.datn_sd31.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "danh_gia")
public class DanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "san_pham_id", nullable = false)
    private SanPham sanPham;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "khach_hang_id", nullable = false)
    private KhachHang khachHang;

    @NotNull
    @Column(name = "so_sao", nullable = false)
    private Integer soSao;

    @NotNull
    @Nationalized
    @Lob
    @Column(name = "noi_dung", nullable = false)
    private String noiDung;

    @Nationalized
    @Lob
    @Column(name = "hinh_anh")
    private String hinhAnh;

    @NotNull
    @Column(name = "thoi_gian", nullable = false)
    private Instant thoiGian;

}