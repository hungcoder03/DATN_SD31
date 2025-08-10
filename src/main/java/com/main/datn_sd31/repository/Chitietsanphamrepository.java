package com.main.datn_sd31.repository;

import com.main.datn_sd31.dto.thong_ke_dto.ThongKeSanPhamDTO;
import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.MauSac;
import com.main.datn_sd31.entity.Size;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
//import java.util.function.Predicate;

@Repository
public interface Chitietsanphamrepository extends JpaRepository<ChiTietSanPham,Integer>, JpaSpecificationExecutor<ChiTietSanPham> {
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
            "WHERE ct.maVach = :maVach")
    ChiTietSanPham findByMaVach(@Param("maVach") String maVach);

    Page<ChiTietSanPham> findByDotGiamGia_Id(Integer dotId, Pageable pageable);

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

    @Query("SELECT DISTINCT ctsp.mauSac FROM ChiTietSanPham ctsp WHERE ctsp.sanPham.id = :sanPhamId")
    List<MauSac> findDistinctMauSacBySanPhamId(@Param("sanPhamId") Integer sanPhamId);

    @Query("SELECT DISTINCT ctsp.size FROM ChiTietSanPham ctsp WHERE ctsp.sanPham.id = :sanPhamId AND ctsp.mauSac.ten = :tenMau")
    List<Size> findDistinctSizeBySanPhamIdAndMauSacTen(@Param("sanPhamId") Integer sanPhamId, @Param("tenMau") String tenMau);

    @Query("""
        SELECT ct 
        FROM ChiTietSanPham ct
        JOIN ct.sanPham sp
        JOIN ct.mauSac ms
        JOIN ct.size sz
        WHERE LOWER(CONCAT(sp.ten, ' ', ms.ten, ' ', sz.ten)) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<ChiTietSanPham> searchByKeyword(@Param("keyword") String keyword);

    default List<ChiTietSanPham> searchByKeywordSplit(String keyword) {
        String[] words = keyword.trim().toLowerCase().split("\\s+");
        return findAll((root, query, cb) -> {
            var sp = root.join("sanPham");
            var ms = root.join("mauSac");
            var sz = root.join("size");

            var concat = cb.concat(cb.concat(sp.get("ten"), " "), cb.concat(ms.get("ten"), " "));
            var finalConcat = cb.lower(cb.concat(concat, sz.get("ten")));

            Predicate[] predicates = Arrays.stream(words)
                    .map(w -> cb.like(finalConcat, "%" + w + "%"))
                    .toArray(Predicate[]::new);

            return cb.and(predicates);
        });
    }
}

