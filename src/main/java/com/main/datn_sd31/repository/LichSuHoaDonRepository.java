package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, Integer> {

    LichSuHoaDon findTopByHoaDonOrderByIdDesc(HoaDon hoaDon);

    List<LichSuHoaDon> findLichSuHoaDonsByHoaDon(HoaDon hoaDon);

    List<LichSuHoaDon> findLichSuHoaDonsByHoaDonOrderByNgayTaoDesc(HoaDon hoaDon);

    @Query("""
    SELECT ls FROM LichSuHoaDon ls
    JOIN ls.hoaDon hd
    WHERE hd.ma = :ma
    ORDER BY ls.ngayTao DESC
    """)
    List<LichSuHoaDon> findByMaHoaDonDesc(@Param("ma") String ma);

    boolean existsByHoaDonAndTrangThai(HoaDon hoaDon, int value);
}


