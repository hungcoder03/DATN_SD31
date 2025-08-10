package com.main.datn_sd31.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.main.datn_sd31.entity.PhieuGiamGia;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PhieuGiamGiaService {
    List<PhieuGiamGia> findAll();
    PhieuGiamGia findById(Integer id);
    List<PhieuGiamGia> findByFilter(LocalDate startDate, LocalDate endDate, String status);
    void save(PhieuGiamGia pg);
    void delete(Integer id);

    BigDecimal tinhTienGiam(String maPhieu, BigDecimal tongTien);

    List<PhieuGiamGia> findAllStatusTrue();


}