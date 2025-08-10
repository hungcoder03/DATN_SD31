package com.main.datn_sd31.entity;

import com.main.datn_sd31.validator.ValidDateRange;
import com.main.datn_sd31.validator.ValidMucDoGiamGia;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

//import com.main.datn_sd31.validator.ValidDateRange;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@ValidDateRange(message = "Ngày bắt đầu không được lớn hơn ngày kết thúc")
@ValidMucDoGiamGia(message = "Mức giảm không hợp lệ với Loại phiếu giảm giá")
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

    @NotNull(message = "Chọn loại phiếu giảm giá")
    @Column(name = "loai_phieu_giam_gia", nullable = false)
    private Integer loaiPhieuGiamGia;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "ngay_ket_thuc")
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate ngayKetThuc;

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public void setKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    @NotNull(message = "Mức giảm không được để trống")
//    @DecimalMin(value = "0", inclusive = true, message = "Mức giảm phải lớn hơn hoặc bằng 0")
//    @DecimalMax(value = "100", message = "Mức giảm tối đa là 100%")
    @Column(name = "muc_do", nullable = false, precision = 18, scale = 0)
    private BigDecimal mucDo;

    @NotNull(message = "Giảm tối đa không được để trống")
    @DecimalMin(value = "0", inclusive = true, message = "Giảm tối đa phải lớn hơn hoặc bằng 0")
    @DecimalMax(value = "500000", message = "Giảm tối đa không được vượt quá 500.000đ")
    @Column(name = "giam_toi_da", nullable = false, precision = 18, scale = 0)
    private BigDecimal giamToiDa;

    @NotNull(message = "Điều kiện không được để trống")
    @DecimalMin(value = "0", message = "Điều kiện không được nhỏ hơn 0")
    @DecimalMax(value = "5000000", message = "Điều kiện không được vượt quá 5.000.000đ")
    @Column(name = "dieu_kien", nullable = false, precision = 18, scale = 0)
    private BigDecimal dieuKien;

    @NotNull(message = "Số lượng tồn không được để trống")
    @Min(value = 0, message = "Số lượng tồn không được nhỏ hơn 0")
    @Max(value = 100000, message = "Số lượng mã không được vượt quá 100.000")
    @Column(name = "so_luong_ton", nullable = false)
    private Integer soLuongTon;

    @OneToMany(mappedBy = "phieuGiamGia")
    private Set<HoaDon> hoaDons = new LinkedHashSet<>();

}