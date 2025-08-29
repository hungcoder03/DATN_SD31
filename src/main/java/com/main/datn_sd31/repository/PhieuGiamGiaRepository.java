package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Integer> {
    @Query("""
    select n from PhieuGiamGia n
    where n.ma=:maPhieu and n.trangThai = true
    """)
    PhieuGiamGia findByMa(String maPhieu);

    @Query("""
    select n from PhieuGiamGia n
    where n.trangThai = true
    """)
    List<PhieuGiamGia> findAllStatusTrue();

    boolean existsByMa(String maPhieu);
}
