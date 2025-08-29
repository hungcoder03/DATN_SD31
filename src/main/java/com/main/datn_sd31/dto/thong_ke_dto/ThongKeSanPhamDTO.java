package com.main.datn_sd31.dto.thong_ke_dto;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@Builder
public class ThongKeSanPhamDTO {

    private Integer id;
    private String maSp;
    private String tenCt;
    private Long soLuongDaBan;
    private Integer soLuongTon;

    public ThongKeSanPhamDTO(Integer id, String maSp, String tenCt, Long soLuongDaBan, Integer soLuongTon) {
        this.id = id;
        this.maSp = maSp;
        this.tenCt = tenCt;
        this.soLuongDaBan = soLuongDaBan;
        this.soLuongTon = soLuongTon;
    }

}
