package com.main.datn_sd31.service;

import com.main.datn_sd31.entity.LichSuHoaDon;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LichSuHoaDonService {

    List<LichSuHoaDon> getLichSuHoaDonByHoaDon(String maHoaDon);

    void capNhatTrangThai(String maHoaDon, Integer trangThaiMoi, String ghiChu, boolean isRollback);

}