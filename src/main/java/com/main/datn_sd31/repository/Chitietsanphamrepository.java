package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.ChiTietSanPham;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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


}

