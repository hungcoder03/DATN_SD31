package com.main.datn_sd31.Enum;

public enum LyDoGiaoKhongThanhCong {
    SHIPPER_MAT_DON(1,"Shipper làm mất đơn"),
    KHACH_KHONG_LIEN_LAC_DUOC(2,"Khách không liên lạc được"),
    KHACH_TU_CHOI_NHAN(3,"Khách từ chối nhận hàng"),
    DIA_CHI_KHONG_HOP_LE(4,"Địa chỉ giao hàng không hợp lệ"),
    HANG_HOA_HU_HONG(5,"Hàng hóa bị hư hỏng trong quá trình vận chuyển"),
    DICH_BENH_HOAC_THIEN_TAI(6,"Do dịch bệnh hoặc thiên tai"),
    SAI_SAN_PHAM(7,"Sai sản phẩm");
//    KHAC(6,"Lý do khác");

    private final int value;
    private final String moTa;

    LyDoGiaoKhongThanhCong(int value, String moTa) {
        this.value = value;
        this.moTa = moTa;
    }

    public int getValue() {
        return value;
    }

    public String getMoTa() {
        return moTa;
    }

    public static LyDoGiaoKhongThanhCong fromValue(int value) {
        for (LyDoGiaoKhongThanhCong tt : values()) {
            if (tt.value == value) {
                return tt;
            }
        }
        return null;
    }
}