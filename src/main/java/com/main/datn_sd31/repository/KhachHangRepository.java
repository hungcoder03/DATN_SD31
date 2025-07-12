
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
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    @Query("select n from KhachHang n where n.id=:id")
    KhachHang find(Integer id);

    Optional<KhachHang> findByEmail(String email);

    List<KhachHang> findByMa(String ma);
    @Query("SELECT k FROM KhachHang k WHERE k.soDienThoai LIKE %:sdt%")
    List<KhachHang> findBySoDienThoaiContaining(@Param("sdt") String sdt);

//    @Query("select k from KhachHang k where k.ma like %:search% or k.ten like %:search% or k.soDienThoai like %:search% or k.email like %:search%")
//    List<KhachHang> search(@Param("search") String search);

}
