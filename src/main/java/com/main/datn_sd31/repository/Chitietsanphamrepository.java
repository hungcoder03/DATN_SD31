package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.SanPham;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface Chitietsanphamrepository extends JpaRepository<ChiTietSanPham,Integer> {
    @Query("""
        select n from ChiTietSanPham n where n.sanPham.id = :id
    """)
    List<ChiTietSanPham> findBySanPhamId(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("""
        delete from ChiTietSanPham n where n.sanPham.id = :id
    """)
    int findBydeleteid(@Param("id") Integer id);

    @Query("""
        select n from ChiTietSanPham n where n.sanPham.id = :sanphamId and n.size.id = :sizeId and n.mauSac.id = :mauSacId
    """)
    ChiTietSanPham findBySanPhamIdAndSizeIdAndMauSacId(Integer sanphamId, Integer sizeId, Integer mauSacId);


    @Query("select n from ChiTietSanPham n where n.sanPham.id =:spId")
    ChiTietSanPham find(Integer spId);
    @Query("SELECT ctsp FROM ChiTietSanPham ctsp " +
            "WHERE (LOWER(ctsp.sanPham.ten) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR ctsp.maVach LIKE CONCAT('%', :keyword, '%')) " +
            "AND ctsp.soLuong > 0")
    List<ChiTietSanPham> findByTenSanPhamContainingIgnoreCase(@Param("keyword") String keyword);



    @Query("""
    SELECT ctsp FROM ChiTietSanPham ctsp
    JOIN FETCH ctsp.mauSac
    JOIN FETCH ctsp.size
    JOIN FETCH ctsp.sanPham
    WHERE ctsp.id = :id
""")
    ChiTietSanPham findWithDetailsById(@Param("id") Integer id);

    @Query("SELECT ctsp FROM ChiTietSanPham ctsp " +
            "JOIN ctsp.sanPham sp " +
            "JOIN ctsp.mauSac ms " +
            "JOIN ctsp.size sz " +
            "WHERE sp.ten LIKE %:keyword% OR sp.ma LIKE %:keyword%")
    List<ChiTietSanPham> findByTenOrMa(@Param("keyword") String keyword);


    // ChiTietSanPhamRepository.java
    @Query("SELECT ct FROM ChiTietSanPham ct " +
            "WHERE ct.id = :maVach")
    ChiTietSanPham findByMaVach(@Param("maVach") String maVach);
    @Query("""
    SELECT
        ctsp.id,
        ctsp.tenCt,
        MAX(hdct.tenCtsp),
        COALESCE(SUM(hdct.soLuong), 0),
        ctsp.soLuong
    FROM ChiTietSanPham ctsp
    LEFT JOIN HoaDonChiTiet hdct
      ON hdct.chiTietSanPham = ctsp
      AND hdct.ngayTao BETWEEN :startDate AND :endDate
    GROUP BY ctsp.id, ctsp.tenCt, ctsp.soLuong
    HAVING MAX(hdct.tenCtsp) IS NOT NULL AND MAX(hdct.tenCtsp) <> ''
    ORDER BY ctsp.id
    """)
    List<Object[]> getRawThongKeSanPhamTheoKhoangNgay(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

}

