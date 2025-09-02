package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.BanDich;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BanDichRepository extends JpaRepository<BanDich, Integer> {
    
    Optional<BanDich> findByTenKhoaAndMaNgonNgu(String tenKhoa, String maNgonNgu);
    
    List<BanDich> findByMaNgonNgu(String maNgonNgu);
    
    List<BanDich> findByDanhMuc(String danhMuc);
    
    @Query("SELECT b FROM BanDich b WHERE b.maNgonNgu = :maNgonNgu AND b.danhMuc = :danhMuc")
    List<BanDich> findByMaNgonNguAndDanhMuc(@Param("maNgonNgu") String maNgonNgu, 
                                           @Param("danhMuc") String danhMuc);
    
    @Query("SELECT b FROM BanDich b WHERE b.tenKhoa = :tenKhoa")
    List<BanDich> findAllByTenKhoa(@Param("tenKhoa") String tenKhoa);
    
    boolean existsByTenKhoaAndMaNgonNgu(String tenKhoa, String maNgonNgu);
    
    @Query("SELECT b FROM BanDich b WHERE b.duocTaoBoiAi = true")
    List<BanDich> findAllAiGenerated();
}
