package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Giohangreposiroty extends JpaRepository<GioHangChiTiet,Integer> {
    @Query("select n from GioHangChiTiet n where n.id=:id")
    GioHangChiTiet find(Integer id);


    @Query("SELECT g FROM GioHangChiTiet g WHERE g.id IN :ids")
    List<GioHangChiTiet> findByIdIn(@Param("ids") List<Integer> ids);

    @Query("SELECT g.chiTietSp.id, g.chiTietSp.size.id, g.chiTietSp.mauSac.id, SUM(g.soLuong), SUM(g.thanhTien) " +
            "FROM GioHangChiTiet g " +
            "GROUP BY g.chiTietSp.id, g.chiTietSp.size.id, g.chiTietSp.mauSac.id " +
            "HAVING COUNT(g) > 1")
    List<Object[]> findGroupsToMerge();

    // Lấy tất cả bản ghi theo chiTietSanPham + size + mausac
    List<GioHangChiTiet> findByChiTietSp_IdAndChiTietSp_Size_IdAndChiTietSp_MauSac_Id(
            Integer idSp, Integer idSize, Integer idMauSac);

    @Query("SELECT ghct FROM GioHangChiTiet ghct WHERE ghct.khachHang.id = :khachHangId")
    List<GioHangChiTiet> findByKhachHangId(@Param("khachHangId") Integer khachHangId);

    GioHangChiTiet findByKhachHangIdAndChiTietSpId(Integer id, Integer id1);
}
