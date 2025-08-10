package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.dto.hoa_don_dto.HoaDonChiTietDTO;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.HoaDonChiTiet;
import com.main.datn_sd31.entity.LichSuHoaDon;
import com.main.datn_sd31.repository.HoaDonChiTietRepository;
import com.main.datn_sd31.service.HoaDonChiTietService;
import com.main.datn_sd31.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoaDonChiTietServiceIpml implements HoaDonChiTietService {

    private final HoaDonChiTietRepository hoaDonChiTietRepository;

    private HoaDonChiTietDTO mapToDTO(HoaDonChiTiet hoaDonChiTiet) {
        return HoaDonChiTietDTO.builder()
                .idHoaDonChiTiet(hoaDonChiTiet.getId())
                .maHD(hoaDonChiTiet.getHoaDon().getMa())
                .idCTSP(hoaDonChiTiet.getChiTietSanPham().getId())
                .tenCTSP(hoaDonChiTiet.getTenCtsp())
                .soLuong(hoaDonChiTiet.getSoLuong())
                .giaSauGiam(hoaDonChiTiet.getGiaSauGiam())
                .giaGiam(hoaDonChiTiet.getGiaGiam())
                .giaGoc(hoaDonChiTiet.getGiaGoc())
                .tongTien(hoaDonChiTiet.getGiaSauGiam().multiply(BigDecimal.valueOf(hoaDonChiTiet.getSoLuong())))
                .build();
    }

    private List<HoaDonChiTietDTO> mapToDTOs(List<HoaDonChiTiet> entities) {
        return entities.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    @Override
    public List<HoaDonChiTietDTO> getHoaDonChiTietByMaHoaDon(String maHoaDon) {
        return mapToDTOs(hoaDonChiTietRepository.findByHoaDon(maHoaDon));
    }

    @Override
    public HoaDonChiTietDTO capNhatSoLuong(Integer id, Integer soLuongMoi) {
        HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(id).orElseThrow();
        hdct.setSoLuong(soLuongMoi);
        hdct.setNgaySua(LocalDateTime.now());
//        hdct.setTongTien(hdct.getGiaSauGiam().multiply(BigDecimal.valueOf(soLuongMoi)));
        hoaDonChiTietRepository.save(hdct);
        return mapToDTO(hdct);
    }
}