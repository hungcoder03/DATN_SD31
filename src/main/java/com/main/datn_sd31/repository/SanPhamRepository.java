package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    
    // Chỉ lấy sản phẩm đang hoạt động
    List<SanPham> findByTrangThaiTrue();
    
    // Chỉ lấy sản phẩm đang hoạt động theo tên
    List<SanPham> findByTrangThaiTrueAndTenContainingIgnoreCase(String keyword);

    // Chỉ lấy sản phẩm đang hoạt động cho search
    @Query("SELECT sp FROM SanPham sp " +
            "WHERE sp.trangThai = true " +
            "AND LOWER(sp.ten) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<SanPham> search(@Param("search") String search);

    // Chỉ lấy sản phẩm đang hoạt động cho filter
    @Query("""
        SELECT DISTINCT sp
        FROM SanPham sp
        JOIN sp.chiTietSanPhams ct
        WHERE sp.trangThai = true
          AND LOWER(sp.ten) LIKE LOWER(CONCAT('%', COALESCE(:q, ''), '%'))
          AND (:danhMucId IS NULL OR sp.danhMuc.id = :danhMucId)
          AND (:loaiThuId IS NULL OR sp.loaiThu.id = :loaiThuId)
          AND (:chatLieuId IS NULL OR sp.chatLieu.id = :chatLieuId)
          AND (:kieuDangId IS NULL OR sp.kieuDang.id = :kieuDangId)
          AND (:xuatXuId IS NULL OR sp.xuatXu.id = :xuatXuId)
          AND (:minPrice IS NULL OR ct.giaBan >= :minPrice)
          AND (:maxPrice IS NULL OR ct.giaBan <= :maxPrice)
        """)
    List<SanPham> filter(
            @Param("q") String q,
            @Param("danhMucId") Integer danhMucId,
            @Param("loaiThuId") Integer loaiThuId,
            @Param("chatLieuId") Integer chatLieuId,
            @Param("kieuDangId") Integer kieuDangId,
            @Param("xuatXuId") Integer xuatXuId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    // Kiểm tra mã sản phẩm có tồn tại không
    boolean existsByMa(String ma);

    @Query("SELECT sp FROM SanPham sp JOIN sp.chiTietSanPhams ctsp WHERE ctsp = :chiTietSanPham")
    SanPham findSanPhamByChiTietSanPham(@Param("chiTietSanPham") ChiTietSanPham chiTietSanPham);
}
