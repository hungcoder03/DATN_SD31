package com.main.datn_sd31.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Component("utils")
public class ThymleafHelper {
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount);
    }
}
