package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.LichSuHoaDon;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, Integer> {

    LichSuHoaDon findTopByHoaDonOrderByIdDesc(HoaDon hoaDon);

    List<LichSuHoaDon> findLichSuHoaDonsByHoaDon(HoaDon hoaDon);

}


