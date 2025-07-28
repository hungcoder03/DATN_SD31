package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.GioHangChiTiet;
import com.main.datn_sd31.entity.HoaDon;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
        ORDER BY hd.ngayTao Desc
    """)
    Page<HoaDon> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
    SELECT hd FROM HoaDon hd 
    WHERE LOWER(hd.ma) LIKE LOWER(CONCAT('%', :ma, '%'))
    ORDER BY hd.ngayTao Desc
""")
    List<HoaDon> findByMaContainingIgnoreCase(@Param("ma") String ma);


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

    HoaDon getHoaDonByMa(String ma);

    @Query("""
    SELECT hd FROM HoaDon hd
    WHERE (hd.ngayThanhToan >= :startOfDay
      AND hd.ngayThanhToan < :startOfNextDay)
      AND hd.trangThai = :trangThai
    """)
    List<HoaDon> findHoaDonByNgayAndTrangThai(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay,
            @Param("trangThai") Integer trangThai

    );

    @Transactional
    @Modifying
    @Query("UPDATE HoaDon h SET h.trangThai = :trangThai WHERE h.ma = :maHoaDon")
    void capNhatTrangThaiHoaDon(@Param("trangThai") int trangThai, @Param("maHoaDon") String maHoaDon);


}
