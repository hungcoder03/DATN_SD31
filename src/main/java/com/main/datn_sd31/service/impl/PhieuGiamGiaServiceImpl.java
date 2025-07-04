package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.PhieuGiamGia;
import com.main.datn_sd31.dto.phieu_giam_gia.PhieuGiamGiaDto;
import com.main.datn_sd31.repository.PhieuGiamGiaRepository;
import com.main.datn_sd31.service.PhieuGiamGiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhieuGiamGiaServiceImpl implements PhieuGiamGiaService {

    private final PhieuGiamGiaRepository repository;

    @Override
    public List<PhieuGiamGia> findAll() {
        List<PhieuGiamGia> entities = repository.findAll();
        return entities.isEmpty() ? List.of() : entities;
    }

    @Override
    public List<PhieuGiamGia> findByFilter(LocalDate startDate, LocalDate endDate, String status) {
        List<PhieuGiamGia> all = repository.findAll();

        return all.stream()
                .filter(p -> startDate == null || (p.getNgayBatDau() != null && !p.getNgayBatDau().isBefore(startDate)))
                .filter(p -> endDate == null || (p.getNgayKetThuc() != null && !p.getNgayKetThuc().isAfter(endDate)))
                .filter(p -> {
                    if (status == null || status.isEmpty()) return true;
                    LocalDate now = LocalDate.now();
                    if ("hoatdong".equals(status)) {
                        return (p.getNgayBatDau() != null && p.getNgayKetThuc() != null)
                                && (!now.isBefore(p.getNgayBatDau()) && !now.isAfter(p.getNgayKetThuc()))
                                && (p.getSoLuongTon() != null && p.getSoLuongTon() > 0);
                    } else if ("chuabatdau".equals(status)) {
                        return p.getNgayBatDau() != null && now.isBefore(p.getNgayBatDau());
                    } else if ("ketthuc".equals(status)) {
                        return p.getNgayKetThuc() != null && now.isAfter(p.getNgayKetThuc());
                    } else if ("hethang".equals(status)) {
                        return p.getSoLuongTon() != null && p.getSoLuongTon() == 0;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PhieuGiamGia findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(PhieuGiamGia pg) {
        repository.save(pg);
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
