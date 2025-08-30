package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.Conversation;
import com.main.datn_sd31.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByActiveTrueOrderByCreatedAtAsc();
    long countByAssignedEmployeeAndActiveTrue(NhanVien nhanVien);
} 