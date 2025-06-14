package com.main.datn_sd31.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50, message = "Mã tối đa 50 ký tự")
    @NotNull
    @Nationalized
    @NotBlank(message = "Mã không được để trống")
    @Column(name = "ma", nullable = false, length = 50)
    private String ma;

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 100, message = "Tên tối đa 100 ký tự")
    @NotNull
    @Nationalized
    @Column(name = "ten", nullable = false, length = 100)
    private String ten;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
    private LocalDate ngayTao;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_sua")
    private LocalDate ngaySua;

    @Column(name = "nguoi_tao")
    private Integer nguoiTao;

    @Column(name = "nguoi_sua")
    private Integer nguoiSua;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Boolean trangThai = false;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @NotBlank(message = "Loại phiếu không được để trống")
    @Column(name = "loai_phieu_giam_gia", nullable = false, length = 50)
    private String loaiPhieuGiamGia;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "ngay_ket_thuc")
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate ngayKetThuc;

    @NotNull(message = "Mức giảm không được để trống")
    @DecimalMin(value = "0", inclusive = true, message = "Mức giảm phải lớn hơn hoặc bằng 0")
    @Column(name = "muc_do", nullable = false, precision = 18, scale = 2)
    private BigDecimal mucDo;

    @NotNull(message = "Giảm tối đa không được để trống")
    @Column(name = "giam_toi_da", nullable = false, precision = 18, scale = 2)
    private BigDecimal giamToiDa;

    @NotNull(message = "Điều kiện không được để trống")
    @DecimalMin(value = "0", message = "Điều kiện không được nhỏ hơn 0")
    @Column(name = "dieu_kien", nullable = false, precision = 18, scale = 2)
    private BigDecimal dieuKien;

    @NotNull(message = "Điều kiện không được để trống")
    @DecimalMin(value = "0", message = "Điều kiện không được nhỏ hơn 0")
    @Column(name = "so_luong_ton", nullable = false)
    private Integer soLuongTon;

    @OneToMany(mappedBy = "phieuGiamGia")
    private Set<HoaDon> hoaDons = new LinkedHashSet<>();

}