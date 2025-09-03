package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.KhachHang;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    @Query("select k from KhachHang k where k.id = :id")
    KhachHang find(@Param("id") Integer id);
    KhachHang findTopByOrderByMaDesc();
    Optional<KhachHang> findByEmail(String email);
    Optional<KhachHang> findBySoDienThoai(String soDienThoai);
    List<KhachHang> findByMa(String ma);
    List<KhachHang> findBySoDienThoaiContaining(String sdt);

    // thêm phương thức search đơn giản giống NhanVien
    @Query("select k from KhachHang k " +
            "where k.ten like %:search% " +
            "   or k.soDienThoai like %:search% " +
            "   or k.email like %:search%")
    List<KhachHang> search(@Param("search") String search);

    @Query("select n from KhachHang n where n.email=:email")
    KhachHang findByEmaill(String email);

    @Query("select n from KhachHang n where n.soDienThoai=:sdt")
    KhachHang findSoDienThoai(String sdt);

    boolean existsBySoDienThoai(@Size(max = 20) String soDienThoai);


    // Thêm vào KhachHangRepository.java
    boolean existsBySoDienThoaiAndIdNot(String soDienThoai, Integer id);

    @Query("SELECT CASE WHEN COUNT(k) > 0 THEN true ELSE false END FROM KhachHang k WHERE k.soDienThoai = :soDienThoai")
    boolean existsBySoDienThoaiCustom(@Param("soDienThoai") String soDienThoai);
}