package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.PhieuGiamGia;
import com.main.datn_sd31.dto.phieu_giam_gia.PhieuGiamGiaDto;
import com.main.datn_sd31.repository.PhieuGiamGiaRepository;
import com.main.datn_sd31.service.PhieuGiamGiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhieuGiamGiaServiceImpl implements PhieuGiamGiaService {

    private final PhieuGiamGiaRepository repository;

    @Override
    public List<PhieuGiamGiaDto> findAll() {
        List<PhieuGiamGia> entities = repository.findAll();
        List<PhieuGiamGiaDto> dtos = entities.stream()
                .map(entity -> new PhieuGiamGiaDto(
                        entity.getId(),
                        entity.getMa(),
                        entity.getTen(),
                        entity.getLoaiPhieuGiamGia(),
                        entity.getNgayBatDau(),
                        entity.getNgayKetThuc(),
                        entity.getMucDo(),
                        entity.getGiamToiDa(),
                        entity.getDieuKien(),
                        entity.getSoLuongTon()
                ))
                .collect(Collectors.toList());

        return dtos;
    }

    @Override
    public PhieuGiamGiaDto findDtoById(Integer id) {
        Optional<PhieuGiamGia> optional = repository.findById(id);
        return optional.map(entity -> new PhieuGiamGiaDto(
                        entity.getId(),
                        entity.getMa(),
                        entity.getTen(),
                        entity.getLoaiPhieuGiamGia(),
                        entity.getNgayBatDau(),
                        entity.getNgayKetThuc(),
                        entity.getMucDo(),
                        entity.getGiamToiDa(),
                        entity.getDieuKien(),
                        entity.getSoLuongTon()
                ))
                .orElse(null);
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