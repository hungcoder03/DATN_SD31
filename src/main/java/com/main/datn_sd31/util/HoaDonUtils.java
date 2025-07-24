package com.main.datn_sd31.util;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import org.springframework.stereotype.Component;

@Component("hoa_don_utils")
public class HoaDonUtils {

    public static boolean choPhepSuaGhiChuHoaDon(TrangThaiLichSuHoaDon trangThai) {
        return switch (trangThai) {
            case CHO_XAC_NHAN -> true;
            case XAC_NHAN -> true; // nếu nhân viên cần hỗ trợ sửa
            default -> false;
        };
    }

    public static boolean khongChoPhepCapNhatTrangThai(TrangThaiLichSuHoaDon trangThai) {
        return switch (trangThai) {
            case HOAN_THANH, DA_HOAN, HUY -> false;
            default -> true;
        };
    }

    public static boolean choPhepInHoaDon(TrangThaiLichSuHoaDon trangThai, Integer thanhToan) {
        if (thanhToan == 3) {
            return switch (trangThai) {
                case HOAN_THANH -> true;
                default -> false;
            };
        }
        return false;
    }
}