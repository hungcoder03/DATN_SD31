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
@Table(name = "cache_ban_dich_ai",
       uniqueConstraints = @UniqueConstraint(columnNames = {"van_ban_nguon", "ngon_ngu_nguon", "ngon_ngu_dich"}))
public class CacheBanDichAi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Nationalized
    @Lob
    @Column(name = "van_ban_nguon", nullable = false)
    private String vanBanNguon;

    @Size(max = 5)
    @NotNull
    @Column(name = "ngon_ngu_nguon", nullable = false, length = 5)
    private String ngonNguNguon;

    @Size(max = 5)
    @NotNull
    @Column(name = "ngon_ngu_dich", nullable = false, length = 5)
    private String ngonNguDich;

    @NotNull
    @Nationalized
    @Lob
    @Column(name = "van_ban_da_dich", nullable = false)
    private String vanBanDaDich;

    @Column(name = "diem_tin_cay", precision = 3, scale = 2)
    private BigDecimal diemTinCay;

    @ColumnDefault("1")
    @Column(name = "so_lan_su_dung")
    private Integer soLanSuDung = 1;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "lan_cuoi_su_dung")
    private LocalDateTime lanCuoiSuDung;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        lanCuoiSuDung = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lanCuoiSuDung = LocalDateTime.now();
    }
}
