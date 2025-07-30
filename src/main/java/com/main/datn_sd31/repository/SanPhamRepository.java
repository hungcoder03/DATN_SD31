package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {

    @Query("SELECT sp FROM SanPham sp " +
            "WHERE LOWER(sp.ten) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<SanPham> search(@Param("search") String search);
    @Query("""
        SELECT DISTINCT sp
        FROM SanPham sp
        JOIN sp.chiTietSanPhams ct
        WHERE LOWER(sp.ten) LIKE LOWER(CONCAT('%', :q, '%'))
          AND (:danhMucId IS NULL OR sp.danhMuc.id = :danhMucId)
          AND (:loaiThuId IS NULL OR sp.loaiThu.id = :loaiThuId)       
          AND (
                :minPrice IS NULL OR :maxPrice IS NULL
                OR ct.giaBan BETWEEN :minPrice AND :maxPrice
              )
        """)
    List<SanPham> filter(
            @Param("q") String q,
            @Param("danhMucId") Integer danhMucId,
            @Param("loaiThuId") Integer loaiThuId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );
}

