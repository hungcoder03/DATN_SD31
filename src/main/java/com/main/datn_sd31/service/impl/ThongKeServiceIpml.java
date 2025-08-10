package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.dto.thong_ke_dto.ThongKeSanPhamDTO;
import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.HoaDonChiTiet;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.HoaDonChiTietRepository;
import com.main.datn_sd31.repository.HoaDonRepository;
import com.main.datn_sd31.repository.SanPhamRepository;
import com.main.datn_sd31.service.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThongKeServiceIpml implements ThongKeService {

    private final HoaDonRepository hoaDonRepository;

    private final HoaDonChiTietRepository hoaDonChiTietRepository;

    private final SanPhamRepository SanPhamRepository;

    private final Chitietsanphamrepository chitietsanphamrepository;

    private BigDecimal tongTien(List<HoaDon> list) {
        return list.stream()
                .map(hd -> hd.getThanhTien().subtract(hd.getPhiVanChuyen()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private Integer tongDonHang(List<HoaDon> list) {
        return list.size();
    }

//    private List<HoaDon> getHoaDonHomNay() {
//        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
//        LocalDateTime startOfNextDay = startOfDay.plusDays(1);
//        return hoaDonRepository.findHoaDonByNgayAndTrangThai(startOfDay, startOfNextDay, 3);
//    }

    @Override
    public BigDecimal getDoanhThu(LocalDateTime start, LocalDateTime end, Integer trangThai) {
        List<HoaDon> hoaDonList = hoaDonRepository.findHoaDonByNgayAndTrangThai(start, end, trangThai);
        return tongTien(hoaDonList);
    }

    @Override
    public Integer countDonHang(LocalDateTime start, LocalDateTime end, Integer trangThai) {
        return tongDonHang(hoaDonRepository.findHoaDonByNgayAndTrangThai(start, end, trangThai));
    }

    @Override
    public Integer getTongSanPham(LocalDateTime start, LocalDateTime end) {
        List<HoaDonChiTiet> hoaDonChiTietList = hoaDonChiTietRepository.findHoaDonByNgay(start, end);
        return hoaDonChiTietList.stream()
                .map(HoaDonChiTiet::getSoLuong)
                .reduce(0, Integer::sum);
    }

    @Override
    public List<ThongKeSanPhamDTO> getThongKeSanPham(LocalDateTime start, LocalDateTime end) {
        List<Object[]> rawData = chitietsanphamrepository.getRawThongKeSanPhamTheoKhoangNgay(start, end);

        return rawData.stream()
                .map(obj -> new ThongKeSanPhamDTO(
                        (Integer) obj[0],           // id
                        (String) obj[2],            // tenCt
                        (Long) obj[3],              // soLuongDaBan (SUM trả về Long)
                        (Integer) obj[4]            // soLuongTon
                ))
                .collect(Collectors.toList());
    }

}
