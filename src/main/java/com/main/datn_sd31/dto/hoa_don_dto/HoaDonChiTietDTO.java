package com.main.datn_sd31.dto.hoa_don_dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class HoaDonChiTietDTO {

    private Integer idHoaDonChiTiet;
    private String maHD;
    private Integer idCTSP;
    private String tenCTSP;
    private String maSp;
    private BigDecimal giaGoc;
    private Integer soLuong;
    private BigDecimal giaSauGiam;
    private BigDecimal giaGiam;
    private BigDecimal tongTien;

}
