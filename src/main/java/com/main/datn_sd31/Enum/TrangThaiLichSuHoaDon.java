package com.main.datn_sd31.Enum;

import jakarta.persistence.Transient;

public enum TrangThaiLichSuHoaDon {
    CHO_XAC_NHAN(1, "Chờ xác nhận"),
    XAC_NHAN(2, "Xác nhận"),
    CHO_GIAO_HANG(3, "Chờ giao hàng"),
    DA_GIAO(4, "Đã giao"),
    HOAN_THANH(5, "Hoàn thành"), //Sau 3 ngày giao thành công sẽ chuyển sang hoàn thành

    YEU_CAU_HOAN_HANG(6, "Yêu cầu hoàn hàng"),
    XAC_NHAN_HOAN_HANG(7, "Xác nhận hoàn hàng"),
    DA_HOAN(8, "Đã hoàn"),

    HUY(9, "Hủy"),
    GIAO_KHONG_THANH_CONG(10, "Giao không thành công");

    private final int value;
    private final String moTa;

    TrangThaiLichSuHoaDon(int value, String moTa) {
        this.value = value;
        this.moTa = moTa;
    }

    public int getValue() {
        return value;
    }

    public String getMoTa() {
        return moTa;
    }

    public static TrangThaiLichSuHoaDon fromValue(int value) {
        for (TrangThaiLichSuHoaDon tt : values()) {
            if (tt.value == value) {
                return tt;
            }
        }
        return null;
    }

    public String getIconTrangThai() {
        return switch (this.value) {
            case 1 -> "bi bi-hourglass-split";
            case 2 -> "bi bi-check-square";
            case 3 -> "bi bi-truck";
            case 4 -> "bi bi-box-seam";
            case 5 -> "bi bi-check-circle";
            case 6 -> "bi bi-arrow-counterclockwise";
            case 7 -> "bi bi-arrow-repeat";
            case 8 -> "bi bi-box-arrow-in-left";
            case 9 -> "bi bi-x-circle";
            default -> "bi bi-question-circle";
        };
    }

}