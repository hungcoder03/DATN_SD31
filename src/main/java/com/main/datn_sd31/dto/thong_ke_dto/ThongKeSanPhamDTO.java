package com.main.datn_sd31.dto.thong_ke_dto;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@Builder
public class ThongKeSanPhamDTO {

    private Integer id;
    private String tenCt;
    private Long soLuongDaBan;
    private Integer soLuongTon;

    public ThongKeSanPhamDTO(Integer id, String tenCt, Long soLuongDaBan, Integer soLuongTon) {
        this.id = id;
        this.tenCt = tenCt;
        this.soLuongDaBan = soLuongDaBan;
        this.soLuongTon = soLuongTon;
    }

}
