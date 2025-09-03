package com.main.datn_sd31.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "ngon_ngu")
public class NgonNgu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 5)
    @NotNull
    @Column(name = "ma_ngon_ngu", nullable = false, length = 5, unique = true)
    private String maNgonNgu;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "ten_ngon_ngu", nullable = false, length = 50)
    private String tenNgonNgu;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "ten_ban_dia", nullable = false, length = 50)
    private String tenBanDia;

    @Size(max = 255)
    @Column(name = "duong_dan_co", length = 255)
    private String duongDanCo;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai = true;

    @ColumnDefault("0")
    @Column(name = "mac_dinh")
    private Boolean macDinh = false;

    @Column(name = "thu_tu")
    private Integer thuTu = 0;

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
