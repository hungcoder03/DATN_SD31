package com.main.datn_sd31.dto.thong_ke_dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCustomerDTO {
	private Integer id;
	private String ten;
	private Long soDon;
	private BigDecimal tongChi;
} 