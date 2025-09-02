package com.main.datn_sd31.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ban_dich", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"ten_khoa", "ma_ngon_ngu"}))
public class BanDich {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "ten_khoa", nullable = false, length = 255)
    private String tenKhoa;

    @Size(max = 5)
    @NotNull
    @Column(name = "ma_ngon_ngu", nullable = false, length = 5)
    private String maNgonNgu;

    @NotNull
    @Nationalized
    @Lob
    @Column(name = "noi_dung", nullable = false)
    private String noiDung;

    @Size(max = 100)
    @Column(name = "danh_muc", length = 100)
    private String danhMuc;

    @ColumnDefault("0")
    @Column(name = "duoc_tao_boi_ai")
    private Boolean duocTaoBoiAi = false;

    @Column(name = "diem_tin_cay", precision = 3, scale = 2)
    private BigDecimal diemTinCay;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngaySua = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ngaySua = LocalDateTime.now();
    }
}
