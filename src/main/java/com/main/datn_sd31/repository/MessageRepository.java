package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.Conversation;
import com.main.datn_sd31.entity.Message;
import com.main.datn_sd31.Enum.MessageSenderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findTop50ByConversationOrderByCreatedAtDesc(Conversation conversation);
    Message findTop1ByConversationOrderByCreatedAtDesc(Conversation conversation);
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);

    // Đếm số tin nhắn từ khách hàng trong các cuộc trò chuyện đang hoạt động
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.active = true AND m.senderType = :senderType AND m.isRead = false")
    long countByConversationActiveTrueAndSenderTypeAndIsReadFalse(@Param("senderType") MessageSenderType senderType);

    // Đếm số tin nhắn chưa đọc cho một cuộc trò chuyện cụ thể
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation = :conversation AND m.senderType = :senderType AND m.isRead = false")
    default long countByConversationAndSenderTypeAndIsReadFalse(@Param("conversation") Conversation conversation, @Param("senderType") MessageSenderType senderType) {
        return countByConversationAndSenderTypeAndIsReadFalseImpl(conversation, senderType);
    }
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation = :conversation AND m.senderType = :senderType AND m.isRead = false")
    long countByConversationAndSenderTypeAndIsReadFalseImpl(@Param("conversation") Conversation conversation, @Param("senderType") MessageSenderType senderType);

    // Đánh dấu tất cả tin nhắn trong một cuộc trò chuyện là đã đọc
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversation.id = :conversationId AND m.senderType = :senderType")
    int markAsReadByConversationAndSenderType(@Param("conversationId") Long conversationId, @Param("senderType") MessageSenderType senderType);
} 