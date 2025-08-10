package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.DanhGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {
    List<DanhGia> findBySanPhamIdOrderByThoiGianDesc(Integer sanPhamId);
    Page<DanhGia> findBySanPhamIdOrderByThoiGianDesc(Integer sanPhamId, Pageable pageable);

    boolean existsBySanPhamIdAndKhachHangId(Integer sanPhamId, Integer khachHangId);

}