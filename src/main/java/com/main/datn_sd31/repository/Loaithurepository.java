package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.LoaiThu;
import com.main.datn_sd31.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Loaithurepository extends JpaRepository<LoaiThu,Integer> {
    LoaiThu findTopByOrderByMaDesc();

}
