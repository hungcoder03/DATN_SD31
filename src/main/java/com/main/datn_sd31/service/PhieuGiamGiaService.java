package com.main.datn_sd31.service;

import com.main.datn_sd31.dto.phieu_giam_gia.PhieuGiamGiaDto;
import com.main.datn_sd31.entity.PhieuGiamGia;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PhieuGiamGiaService {
    List<PhieuGiamGiaDto> findAll();
    PhieuGiamGiaDto findDtoById(Integer id);
    PhieuGiamGia findById(Integer id);
    void save(PhieuGiamGia pg);
    void delete(Integer id);
}