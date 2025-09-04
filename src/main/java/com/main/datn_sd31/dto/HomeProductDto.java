package com.main.datn_sd31.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeProductDto {
    private Integer id;
    private String name;
    private String imageUrl;
    private java.math.BigDecimal price;
    private String priceText;
    private Integer discountPercent;
    private Double ratingAvg;
}
