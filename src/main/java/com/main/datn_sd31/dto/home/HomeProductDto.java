package com.main.datn_sd31.dto.home;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeProductDto {
    private Integer id;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private Integer discountPercent; // nullable
    private Double ratingAvg;        // nullable
    // Pre-formatted price text for UI (e.g., 1.290.000 đ). Nullable when price is null
    private String priceText;
} 