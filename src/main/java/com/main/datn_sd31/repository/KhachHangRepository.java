package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    @Query("select n from KhachHang n where n.id=:id")
    KhachHang find(Integer id);

    Optional<KhachHang> findByEmail(String email);


    @Query("select n from KhachHang n where n.email=:email")
    KhachHang findByEmaill(String email);
}