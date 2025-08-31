package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.ChatSession;
import com.main.datn_sd31.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    
    // Tìm session đang active cho conversation
    Optional<ChatSession> findByConversationAndIsActiveTrue(Conversation conversation);
    
    // Tìm tất cả session của conversation
    List<ChatSession> findByConversationOrderByStartTimeDesc(Conversation conversation);
    
    // Tìm session cuối cùng của conversation
    @Query("SELECT cs FROM ChatSession cs WHERE cs.conversation = :conversation ORDER BY cs.startTime DESC")
    List<ChatSession> findLatestSessionByConversation(@Param("conversation") Conversation conversation);
    
    // Đếm số session đang active của nhân viên
    @Query("SELECT COUNT(cs) FROM ChatSession cs WHERE cs.employee.id = :employeeId AND cs.isActive = true")
    Long countActiveSessionsByEmployee(@Param("employeeId") Integer employeeId);
    
    // Tìm nhân viên có ít session active nhất
    @Query("SELECT cs.employee.id FROM ChatSession cs WHERE cs.isActive = true GROUP BY cs.employee.id ORDER BY COUNT(cs) ASC")
    List<Integer> findEmployeeIdsOrderByActiveSessionCount();
} 