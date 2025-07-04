package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.GioHangChiTiet;
import com.main.datn_sd31.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    Page<HoaDon> findByNgayTaoBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("""
        SELECT hd FROM HoaDon hd 
        WHERE LOWER(hd.ma) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(hd.tenNguoiNhan) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR hd.soDienThoai LIKE CONCAT('%', :keyword, '%')
    """)
    Page<HoaDon> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
    SELECT hd FROM HoaDon hd 
    WHERE LOWER(hd.ma) LIKE LOWER(CONCAT('%', :ma, '%'))
""")
    List<HoaDon> findByMaContainingIgnoreCase(@Param("ma") String ma);

    @Query("select n from GioHangChiTiet n where n.id=:id")
    GioHangChiTiet find(Integer id);

    @Query("select n from GioHangChiTiet n where n.khachHang.id = :username and n.id IN :selectedId ")
    List<GioHangChiTiet> laySanPhamTheoIds(
            @Param("username") Integer username,
            @Param("selectedId") List<Integer> selectedId

    );
    @Query("select n from HoaDon n where n.khachHang.id = :id ")
    List<HoaDon> findbyidkhachhang(
            @Param("id") Integer id
    );

    @Query("SELECT n FROM HoaDon n WHERE n.ma = :orderCode")
    HoaDon findByMa(@Param("orderCode") String orderCode);
}
