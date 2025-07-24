package com.main.datn_sd31.service;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.dto.lich_su_hoa_don_dto.KetQuaCapNhatTrangThai;
import com.main.datn_sd31.dto.lich_su_hoa_don_dto.LichSuHoaDonDTO;
import com.main.datn_sd31.entity.LichSuHoaDon;
import com.main.datn_sd31.entity.NhanVien;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Service
public interface LichSuHoaDonService {

    List<LichSuHoaDon> getLichSuHoaDonByHoaDon(String maHoaDon);

    void capNhatTrangThai(String maHoaDon, Integer trangThaiMoi, String ghiChu, NhanVien nhanVien);

//    TrangThaiLichSuHoaDon getTrangThaiTruocDo(String maHoaDon);

    List<TrangThaiLichSuHoaDon> getTrangThaiTiepTheoHopLe(TrangThaiLichSuHoaDon hienTai, HoaDonDTO hoaDonDTO);

    List<LichSuHoaDonDTO> getLichSuHoaDonDTOByHoaDon(String maHoaDon);

    List<LichSuHoaDonDTO> getLichSuHoaDonDTODescByMaHoaDon(String maHoaDon);

    public KetQuaCapNhatTrangThai xuLyCapNhatTrangThai(
            String maHoaDon,
            Integer trangThaiMoi,
//            Boolean quayLui,
            String ghiChu,
            NhanVien nhanVien
    );

}