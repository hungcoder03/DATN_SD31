package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.MauSac;
import com.main.datn_sd31.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Mausacrepository extends JpaRepository<MauSac,Integer> {
    boolean existsByMa(String ma);
    MauSac findTopByOrderByMaDesc();

}
