package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.Conversation;
import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByActiveTrueOrderByCreatedAtAsc();
    long countByAssignedEmployeeAndActiveTrue(NhanVien nhanVien);
    
    // Thêm method mới để tìm conversation theo khách hàng - MỚI
    Optional<Conversation> findByCustomerAndActiveTrue(KhachHang customer);
    
    // Tìm tất cả conversation của một khách hàng (để xem lịch sử) - MỚI
    List<Conversation> findByCustomerOrderByCreatedAtDesc(KhachHang customer);
    
    // Tìm conversations cần đóng tự động - MỚI
    @Query("SELECT c FROM Conversation c WHERE c.active = true AND c.lastActivity < :expirationDate")
    List<Conversation> findExpiredConversations(@Param("expirationDate") LocalDateTime expirationDate);
    
    // Tìm conversation theo customer_id - MỚI
    @Query("SELECT c FROM Conversation c WHERE c.customer.id = :customerId AND c.active = true")
    Optional<Conversation> findByCustomerIdAndActiveTrue(@Param("customerId") Integer customerId);
}