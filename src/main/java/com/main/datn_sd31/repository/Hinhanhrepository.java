package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.HinhAnh;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Hinhanhrepository extends JpaRepository<HinhAnh,Integer> {
    @Query("""
        select n from HinhAnh n where n.sanPham.id = :id and n.trangThai = true
        order by n.loaiAnh asc, n.ngayTao asc
    """)
    List<HinhAnh> findByhinhanhid(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("""
        delete from HinhAnh n where n.sanPham.id = :id
    """)
    int findBydeleteid(@Param("id") Integer id);
}
