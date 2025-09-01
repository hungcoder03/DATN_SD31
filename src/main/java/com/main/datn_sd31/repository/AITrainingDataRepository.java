package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.AITrainingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AITrainingDataRepository extends JpaRepository<AITrainingData, Long> {
    
    List<AITrainingData> findByIsActiveTrue();
    
    List<AITrainingData> findByCategoryAndIsActiveTrue(String category);
    
    @Query("SELECT DISTINCT t.category FROM AITrainingData t WHERE t.isActive = true")
    List<String> findAllActiveCategories();
    
    @Query("SELECT t FROM AITrainingData t WHERE t.isActive = true AND " +
           "(LOWER(t.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.answer) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<AITrainingData> searchByKeyword(@Param("keyword") String keyword);
} 