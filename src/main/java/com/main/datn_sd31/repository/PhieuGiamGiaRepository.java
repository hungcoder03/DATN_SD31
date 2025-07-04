package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Integer> {
    @Query("select n from PhieuGiamGia n where n.ma=:maPhieu")
    PhieuGiamGia findByMa(String maPhieu);
}
