package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.KieuDang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Kieudangrepository extends JpaRepository<KieuDang,Integer> {

}
