package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.CacheBanDichAi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CacheBanDichAiRepository extends JpaRepository<CacheBanDichAi, Integer> {
    
    Optional<CacheBanDichAi> findByVanBanNguonAndNgonNguNguonAndNgonNguDich(
        String vanBanNguon, String ngonNguNguon, String ngonNguDich);
    
    @Modifying
    @Query("UPDATE CacheBanDichAi c SET c.soLanSuDung = c.soLanSuDung + 1, c.lanCuoiSuDung = :now WHERE c.id = :id")
    void incrementUsageCount(@Param("id") Integer id, @Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM CacheBanDichAi c WHERE c.ngonNguNguon = :ngonNguNguon AND c.ngonNguDich = :ngonNguDich")
    List<CacheBanDichAi> findByNgonNguNguonAndNgonNguDich(@Param("ngonNguNguon") String ngonNguNguon, 
                                                         @Param("ngonNguDich") String ngonNguDich);
    
    @Query("SELECT c FROM CacheBanDichAi c ORDER BY c.soLanSuDung DESC")
    List<CacheBanDichAi> findAllOrderByUsageCount();
    
    @Modifying
    @Query("DELETE FROM CacheBanDichAi c WHERE c.ngayTao < :cutoffDate")
    void deleteOldCacheEntries(@Param("cutoffDate") LocalDateTime cutoffDate);
}
