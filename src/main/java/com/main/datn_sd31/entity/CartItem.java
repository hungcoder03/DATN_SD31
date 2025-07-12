package com.main.datn_sd31.entity;

import java.math.BigDecimal;

public class CartItem {
    private Integer sanPhamId;
    private String ten;
    private BigDecimal gia;
    private int soLuong;

    public CartItem() {}

    public CartItem(Integer sanPhamId, String ten, BigDecimal gia, int soLuong) {
        this.sanPhamId = sanPhamId;
        this.ten = ten;
        this.gia = gia;
        this.soLuong = soLuong;
    }

    // Getters & Setters
}
