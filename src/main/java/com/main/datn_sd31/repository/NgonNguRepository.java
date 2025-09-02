package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.NgonNgu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NgonNguRepository extends JpaRepository<NgonNgu, Integer> {
    
    List<NgonNgu> findByTrangThaiTrueOrderByThuTu();
    
    Optional<NgonNgu> findByMaNgonNgu(String maNgonNgu);
    
    Optional<NgonNgu> findByMacDinhTrue();
    
    boolean existsByMaNgonNgu(String maNgonNgu);
    
    @Query("SELECT n FROM NgonNgu n WHERE n.trangThai = true ORDER BY n.thuTu ASC")
    List<NgonNgu> findAllActiveOrderBySort();
}
