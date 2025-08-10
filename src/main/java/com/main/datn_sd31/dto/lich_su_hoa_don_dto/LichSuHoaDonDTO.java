package com.main.datn_sd31.dto.lich_su_hoa_don_dto;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LichSuHoaDonDTO {

    private LocalDateTime ngayTao;

    private TrangThaiLichSuHoaDon trangThaiLichSuHoaDon;

    private String trangThaiMoTa;

    private String ghiChu;

    private Integer nguoiTao;

    private String tenNguoiTao;

    private Integer lyDoGiaoKhongThanhCong;

}
