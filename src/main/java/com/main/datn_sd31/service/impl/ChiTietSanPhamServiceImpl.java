package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.GioHangChiTiet;
import com.main.datn_sd31.service.ChiTietSanPhamService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChiTietSanPhamServiceImpl implements ChiTietSanPhamService {

    @Override
    public boolean kiemTraTonKho(List<GioHangChiTiet> gioHangChiTiets) {
        for (GioHangChiTiet item : gioHangChiTiets) {
            ChiTietSanPham ctsp = item.getChiTietSp();
            if (item.getSoLuong() > ctsp.getSoLuong()) {
                return false;
            }
        }
        return true;
    }

}
