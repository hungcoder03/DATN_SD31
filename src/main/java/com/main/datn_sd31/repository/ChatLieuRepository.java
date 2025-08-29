package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.ChatLieu;
import com.main.datn_sd31.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLieuRepository extends JpaRepository<ChatLieu,Integer> {
    ChatLieu findTopByOrderByMaDesc();

}
