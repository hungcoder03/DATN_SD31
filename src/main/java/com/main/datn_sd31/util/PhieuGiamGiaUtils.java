package com.main.datn_sd31.util;

import com.main.datn_sd31.entity.PhieuGiamGia;
import com.main.datn_sd31.service.HoaDonService;
import com.main.datn_sd31.service.PhieuGiamGiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("phieu_giam_gia_utils")
@RequiredArgsConstructor
public class PhieuGiamGiaUtils {

    private final HoaDonService hoaDonService;

    private final PhieuGiamGiaService phieuGiamGiaService;

    public boolean khongChoPhepXoaPhieuGiamGia(Integer id) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaService.findById(id);
        return hoaDonService.existsByPhieuGiamGia(phieuGiamGia);
    }

}
