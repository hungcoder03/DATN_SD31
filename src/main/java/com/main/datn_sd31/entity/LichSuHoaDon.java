package com.main.datn_sd31.entity;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "lich_su_hoa_don")
public class LichSuHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nguoi_tao")
    private Integer nguoiTao;

    @Column(name = "nguoi_sua")
    private Integer nguoiSua;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hoa_don_id", nullable = false)
    private HoaDon hoaDon;

    @NotNull
    @Nationalized
    @Lob
    @Column(name = "ghi_chu", nullable = false)
    private String ghiChu;

//    @Column(name = "ly_do_giao_khong_thanh_cong")
//    private Integer lyDoGiaoKhongThanhCong;

    @Transient
    public String getTrangThaiMoTa() {
        TrangThaiLichSuHoaDon enumTrangThai = TrangThaiLichSuHoaDon.fromValue(this.trangThai);
        return (enumTrangThai != null) ? enumTrangThai.getMoTa() : "Không xác định";
    }

    @Transient
    public String getIconTrangThai() {
        return switch (this.trangThai) {
            case 1 -> "bi bi-hourglass-split";        // Chờ xác nhận
            case 2 -> "bi bi-check-square";           // Xác nhận
            case 3 -> "bi bi-truck";                  // Chờ giao hàng
            case 4 -> "bi bi-box-seam";               // Đã giao
            case 5 -> "bi bi-check-circle";           // Hoàn thành
            case 6 -> "bi bi-arrow-counterclockwise"; // Yêu cầu hoàn hàng
            case 7 -> "bi bi-arrow-repeat";           // Xác nhận hoàn hàng
            case 8 -> "bi bi-box-arrow-in-left";      // Đã hoàn
            case 9 -> "bi bi-x-circle";               // Hủy
            default -> "bi bi-question-circle";       // Không xác định
        };
    }

}