package com.main.datn_sd31.dto.lich_su_hoa_don_dto;

import java.util.Objects;

public final class KetQuaCapNhatTrangThai {
    private final boolean thanhCong;
    private final String message;

    public KetQuaCapNhatTrangThai(boolean thanhCong, String message) {
        this.thanhCong = thanhCong;
        this.message = message;
    }

    public boolean thanhCong() {
        return thanhCong;
    }

    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (KetQuaCapNhatTrangThai) obj;
        return this.thanhCong == that.thanhCong &&
                Objects.equals(this.message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thanhCong, message);
    }

    @Override
    public String toString() {
        return "KetQuaCapNhatTrangThai[" +
                "thanhCong=" + thanhCong + ", " +
                "message=" + message + ']';
    }

}
