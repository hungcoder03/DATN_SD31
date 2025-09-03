package com.main.datn_sd31.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

// Helper class for payment calculation
@Data
@AllArgsConstructor
public class PaymentCalculation {
    private BigDecimal tongTien;
    private BigDecimal phiShip;
    private BigDecimal giagiam;
    private BigDecimal thanhTien;
}

