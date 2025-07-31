package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Mausacrepository extends JpaRepository<MauSac,Integer> {
    boolean existsByMa(String ma);

}
