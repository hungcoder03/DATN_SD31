package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.LichSuHoaDon;
import com.main.datn_sd31.repository.HoaDonRepository;
import com.main.datn_sd31.repository.LichSuHoaDonRepository;
import com.main.datn_sd31.service.LichSuHoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LichSuHoaDonServiceIpml implements LichSuHoaDonService {

    private final LichSuHoaDonRepository lichSuHoaDonRepository;

    private final HoaDonRepository hoaDonRepository;

    @Override
    public List<LichSuHoaDon> getLichSuHoaDonByHoaDon(String maHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findByMaContainingIgnoreCase(maHoaDon).get(0);
        return lichSuHoaDonRepository.findLichSuHoaDonsByHoaDon(hoaDon);
    }

    @Override
    public void capNhatTrangThai(String maHoaDon, Integer trangThaiMoi, String ghiChu, boolean isRollback) {
        HoaDon hoaDon = hoaDonRepository.findByMaContainingIgnoreCase(maHoaDon).get(0);
        if (hoaDon == null) return;

        List<LichSuHoaDon> lichSuList = lichSuHoaDonRepository.findLichSuHoaDonsByHoaDon(hoaDon);

        Integer trangThaiGhiNhat;
        if (isRollback) {
            if (lichSuList.size() > 1) {
                trangThaiGhiNhat = lichSuList.get(lichSuList.size() - 2).getTrangThai();
            } else {
                return; // Không thể rollback
            }
        } else {
            trangThaiGhiNhat = trangThaiMoi;
        }

        String finalGhiChu = (ghiChu != null && !ghiChu.isBlank())
                ? ghiChu
                : (isRollback ? "Quay lại trạng thái trước đó"
                : "Cập nhật trạng thái: " + TrangThaiLichSuHoaDon.fromValue(trangThaiGhiNhat).getMoTa());

        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .hoaDon(hoaDon)
                .trangThai(trangThaiGhiNhat)
                .ghiChu(finalGhiChu)
                .ngayTao(LocalDateTime.now())
                .build();

        lichSuHoaDonRepository.save(lichSu);
    }
}