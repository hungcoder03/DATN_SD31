package com.main.datn_sd31.Enum;

public enum TrangThaiHoaDon{
//    CHO_THANH_TOAN(1, "Chờ thanh toán"),
    CHUA_THANH_TOAN(2, "Chưa thanh toán"),
    DA_THANH_TOAN(3, "Đã thanh toán"),
    HOAN_HANG(4, "Hoàn hàng"),
    HUY(5, "Hủy");

    private final int value;
    private final String moTa;

    TrangThaiHoaDon(int value, String moTa) {
        this.value = value;
        this.moTa = moTa;
    }

    public int getValue() {
        return value;
    }

    public String getMoTa() {
        return moTa;
    }

    public static TrangThaiHoaDon fromValue(int value) {
        for (TrangThaiHoaDon tt : values()) {
            if (tt.value == value) {
                return tt;
            }
        }
        return null;
    }
}
