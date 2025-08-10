package com.main.datn_sd31.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
@Entity
@Getter
@Setter
@Table(name = "nhan_vien")
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @NotBlank(message = "Mã nhân viên không được để trống")
    @Pattern(regexp = "^NV[0-9]{1,5}$", message = "Mã phải có dạng NV + tối đa 5 chữ số")
    @Nationalized
    @Column(name = "ma", nullable = false, length = 50)
    private String ma;

    @Size(max = 100)
    @NotNull
    @NotBlank(message = "Tên không được để trống")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "Tên chỉ được chứa chữ cái và khoảng trắng")
    @Nationalized
    @Column(name = "ten", nullable = false, length = 100)
    private String ten;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull
    @Column(name = "ngay_sinh", nullable = false)
    private LocalDate ngaySinh;

    @Size(max = 20)
    @NotNull
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại phải đủ 10 chữ số")
    @Nationalized
    @Column(name = "so_dien_thoai", nullable = false, length = 20)
    private String soDienThoai;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "ngay_tham_gia", nullable = false)
    private LocalDate ngayThamGia;

    @Size(max = 20)
    @Nationalized
    @Pattern(regexp = "^\\d{12}$|^$", message = "CMND phải gồm 12 chữ số hoặc để trống")
    @Column(name = "chung_minh_thu", nullable = true, length = 20)
    private String chungMinhThu;

    @NotNull
    @Column(name = "gioi_tinh", nullable = false)
    private Boolean gioiTinh = false;

    @Nationalized
    @Lob
    @Column(name = "anh", nullable = true)
    private String anh;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @NotBlank(message = "Email không được để trống")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$",
            message = "Email phải kết thúc bằng @gmail.com"
    )
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "mat_khau", nullable = false, length = 100)
    private String matKhau;

    @Column(name = "nguoi_tao")
    private Integer nguoiTao;

    @Column(name = "nguoi_sua")
    private Integer nguoiSua;

    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "chuc_vu", nullable = false, length = 50)
    private String chucVu;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Boolean trangThai = false;

    @OneToMany(mappedBy = "nhanVien")
    private Set<HoaDon> hoaDons = new LinkedHashSet<>();

}