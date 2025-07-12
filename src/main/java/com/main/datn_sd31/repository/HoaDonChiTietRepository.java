package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    @Query("""
    SELECT hdct FROM HoaDonChiTiet hdct JOIN hdct.hoaDon hd
    WHERE hd.ma = :ma
""")
    List<HoaDonChiTiet> findByHoaDon(@Param("ma") String ma);


}
