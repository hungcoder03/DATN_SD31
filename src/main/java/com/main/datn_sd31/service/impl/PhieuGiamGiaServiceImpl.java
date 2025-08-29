package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.entity.PhieuGiamGia;
import com.main.datn_sd31.repository.PhieuGiamGiaRepository;
import com.main.datn_sd31.service.PhieuGiamGiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    public void save(PhieuGiamGia pg, NhanVien nhanVien) {
//        pg.setTrangThai(true);
        pg.setNguoiTao(nhanVien.getId());
        repository.save(pg);
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public BigDecimal tinhTienGiam(String maPhieu, BigDecimal tongTien) {
        PhieuGiamGia phieu = repository.findByMa(maPhieu);
        if (phieu == null) return BigDecimal.ZERO;

        // Kiểm tra điều kiện áp dụng (nếu có)
        if (tongTien.compareTo(phieu.getDieuKien()) < 0) {
            return BigDecimal.ZERO;
        }

        // Tính tiền giảm theo loại
        if (phieu.getLoaiPhieuGiamGia()==1) {
            BigDecimal phanTram = phieu.getMucDo(); // Ví dụ: 10 -> 10%
            return tongTien.multiply(phanTram).divide(BigDecimal.valueOf(100));
        } else if (phieu.getLoaiPhieuGiamGia()>0) {
            return phieu.getMucDo(); // Ví dụ: giảm 50k
        }

        return BigDecimal.ZERO;
    }

    @Override
    public List<PhieuGiamGia> findAllStatusTrue() {
        List<PhieuGiamGia> entities = repository.findAllStatusTrue();
        return entities.isEmpty() ? List.of() : entities;
    }

    @Override
    public PhieuGiamGia findByMa(String maGiamGia) {
        return repository.findByMa(maGiamGia);
    }

    @Override
    public void autoUpdateStatus() {
        List<PhieuGiamGia> list = repository.findAll();

        for (PhieuGiamGia pgg : list) {
            if (!pgg.getNgayKetThuc().isAfter(LocalDate.now())) {
                pgg.setTrangThai(false);
                repository.save(pgg);
//                System.out.println("Cap nhat thanh cong");
            }
        }
    }
}