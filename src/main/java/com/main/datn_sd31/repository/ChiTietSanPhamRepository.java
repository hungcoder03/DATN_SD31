package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.ChiTietSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham,Integer> {
    Page<ChiTietSanPham> findByDotGiamGia_Id(Integer dotId, Pageable pageable);
}
