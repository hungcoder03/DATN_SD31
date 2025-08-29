package com.main.datn_sd31.repository;

import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.entity.GioHangChiTiet;
import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.PhieuGiamGia;
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

    Page<HoaDon> findByNgayTaoBetweenOrderByNgayTaoDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("""
    SELECT DISTINCT hd FROM HoaDon hd
    WHERE (:startDate IS NULL OR hd.ngayTao >= :startDate)
      AND (:endDate IS NULL OR hd.ngayTao <= :endDate)
      AND NOT EXISTS (
          SELECT 1 FROM LichSuHoaDon lshd
          WHERE lshd.hoaDon = hd
            AND lshd.trangThai IN (5, 8, 9, 10)
      )
    ORDER BY hd.ngayTao DESC
    """)
    Page<HoaDon> getDonHang(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT hd FROM HoaDon hd
    WHERE (:startDate IS NULL OR hd.ngayTao >= :startDate)
      AND (:endDate IS NULL OR hd.ngayTao <= :endDate)
      AND hd.khachHang.id = :idKhachHang
    ORDER BY hd.ngayTao DESC
    """)
    Page<HoaDon> getDonHangByKhachHang(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("idKhachHang") Integer idKhachHang,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT hd FROM HoaDon hd
    WHERE (:startDate IS NULL OR hd.ngayTao >= :startDate)
      AND (:endDate IS NULL OR hd.ngayTao <= :endDate)
      AND EXISTS (
          SELECT 1 FROM LichSuHoaDon lshd
          WHERE lshd.hoaDon = hd
            AND lshd.trangThai IN (5, 8, 9, 10)
      )
    ORDER BY hd.ngayTao DESC
    """)
    Page<HoaDon> getHoaDon(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );


    @Query("""
        SELECT hd FROM HoaDon hd
        WHERE (LOWER(hd.ma) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(hd.tenNguoiNhan) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR hd.soDienThoai LIKE CONCAT('%', :keyword, '%'))
    """)
    Page<HoaDon> searchByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("""
    SELECT hd FROM HoaDon hd
    WHERE LOWER(hd.ma) LIKE LOWER(CONCAT('%', :ma, '%'))
    ORDER BY hd.ngayTao Desc
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

    HoaDon getHoaDonByMa(String ma);

    @Query("""
    SELECT hd FROM HoaDon hd
    WHERE (hd.ngaySua >= :startOfDay
      AND hd.ngaySua < :startOfNextDay)
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

    boolean existsByPhieuGiamGia(PhieuGiamGia phieuGiamGia);

    @Query("""
    SELECT h FROM HoaDon h 
    WHERE (:loaiHoaDon IS NULL OR h.loaihoadon = :loaiHoaDon)
    AND EXISTS (
      SELECT 1 FROM LichSuHoaDon lshd
      WHERE lshd.hoaDon = h
      AND lshd.trangThai IN (5, 8, 9, 10)
      )
    """)
    Page<HoaDon> searchHoaDonByLoai(@Param("loaiHoaDon") String loaiHoaDon, Pageable pageable);

    @Query("""
    SELECT h FROM HoaDon h 
    WHERE (:loaiHoaDon IS NULL OR h.loaihoadon = :loaiHoaDon)
    AND NOT EXISTS (
      SELECT 1 FROM LichSuHoaDon lshd
      WHERE lshd.hoaDon = h
      AND lshd.trangThai IN (5, 8, 9, 10)
      )
    """)
    Page<HoaDon> searchDonHangByLoai(@Param("loaiHoaDon") String loaiHoaDon, Pageable pageable);

    List<HoaDon> findAllByKhachHang(KhachHang khachHang);

}
