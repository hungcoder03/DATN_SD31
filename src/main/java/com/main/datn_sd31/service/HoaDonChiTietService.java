package com.main.datn_sd31.service;

import com.main.datn_sd31.dto.hoa_don_dto.HoaDonChiTietDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface HoaDonChiTietService {

    List<HoaDonChiTietDTO> getHoaDonChiTietByMaHoaDon(String maHoaDon);

}
