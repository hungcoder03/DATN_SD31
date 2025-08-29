package com.main.datn_sd31.service;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_sd31.dto.Pagination;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.PhieuGiamGia;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public interface HoaDonService {

    Pagination<HoaDonDTO> getAll(Integer pageNo, Integer pageSize, LocalDate startDate, LocalDate endDate);

    List<HoaDonDTO> getAllHoaDon();

    List<HoaDonDTO> getAllHoaDonKhachHang(KhachHang khachHang);

    Map<String, Long> getTrangThaiCount(List<HoaDonDTO> list);

    Pagination<HoaDonDTO> getAllHoaDonByStatus(TrangThaiLichSuHoaDon status, int pageNo, int pageSize);

//    Pagination<HoaDonDTO> searchByKeyword(String keyword, String loaiHoaDon, int pageNo, int pageSize);

    Pagination<HoaDonDTO> searchByKeyword(String keyword, int pageNo, int pageSize);

    HoaDonDTO getHoaDonByMa(String ma);

    void capNhatGhiChuHoaDon(String ma, String ghiChu);

    Pagination<HoaDonDTO> getAllDonHang(Integer pageNo, Integer pageSize, LocalDate startDate, LocalDate endDate);

    Pagination<HoaDonDTO> getAllDonHangByStatus(TrangThaiLichSuHoaDon status, int pageNo, int pageSize);

    Pagination<HoaDonDTO> getAllDonHangKhachHang(KhachHang khachHang, Integer pageNo, Integer pageSize, LocalDate startDate, LocalDate endDate);

    Pagination<HoaDonDTO> getAllHoaDonKhachHangByStatus(KhachHang khachHang, TrangThaiLichSuHoaDon status, int pageNo, int pageSize);

    boolean existsByPhieuGiamGia(PhieuGiamGia phieuGiamGia);

    Pagination<HoaDonDTO> searchByLoaiHoaDon(String loaiHoaDon, int pageNo, int pageSize);

    Pagination<HoaDonDTO> searchByLoaiDonHang(String loaiHoaDon, int pageNo, int pageSize);

}
