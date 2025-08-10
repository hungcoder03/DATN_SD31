package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    @Query("""
    SELECT hdct FROM HoaDonChiTiet hdct JOIN hdct.hoaDon hd
    WHERE hd.ma = :ma
    """)
    List<HoaDonChiTiet> findByHoaDon(@Param("ma") String ma);

    @Query("""
    SELECT hdct FROM HoaDonChiTiet hdct
    WHERE (hdct.ngayTao >= :startOfDay
      AND hdct.ngayTao < :startOfNextDay)
      AND hdct.trangThai = true
    """)
    List<HoaDonChiTiet> findHoaDonByNgay(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay
    );

    @Query("""
    SELECT hdct FROM HoaDonChiTiet hdct
    join ChiTietSanPham ctsp
    WHERE (hdct.ngayTao >= :startOfDay
      AND hdct.ngayTao < :startOfNextDay)
      AND hdct.trangThai = true
    """)
    List<HoaDonChiTiet> findSanPhamChiTietByNgay(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay
    );

}
