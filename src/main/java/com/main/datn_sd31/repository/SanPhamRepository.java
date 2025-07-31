package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    List<SanPham> findByTenContainingIgnoreCase(String keyword);
}
