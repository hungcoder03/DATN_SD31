package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    boolean existsByMa(String ma);

    Optional<NhanVien> findByEmail(String email);

    @Query("select n from NhanVien n where n.id=:id")
    NhanVien find(Integer id);

    @Query("select n from NhanVien n where n.ma like %:search% or n.ten like %:search% or n.soDienThoai like %:search% or n.email like %:search%")
    List<NhanVien> search(@Param("search") String search);

    @Query("SELECT nv FROM NhanVien nv WHERE nv.ma = :ma")
    List<NhanVien> findByMa(@Param("ma") String ma);

    // Kiểm tra trùng lặp cho validation
    boolean existsByEmail(String email);
    boolean existsBySoDienThoai(String soDienThoai);
    boolean existsByChungMinhThu(String chungMinhThu);
    
    // Kiểm tra trùng lặp khi update (loại trừ ID hiện tại)
    boolean existsByEmailAndIdNot(String email, Integer id);
    boolean existsBySoDienThoaiAndIdNot(String soDienThoai, Integer id);
    boolean existsByChungMinhThuAndIdNot(String chungMinhThu, Integer id);
    boolean existsByMaAndIdNot(String ma, Integer id);
}